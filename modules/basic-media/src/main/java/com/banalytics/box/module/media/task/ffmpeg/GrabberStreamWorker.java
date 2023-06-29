package com.banalytics.box.module.media.task.ffmpeg;

import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.media.preprocessor.BanalyticsWatermarkPreprocessor;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.Arrays;
import java.util.TimerTask;

import static com.banalytics.box.BanalyticsBoxInstanceState.getInstance;
import static com.banalytics.box.module.ExecutionContext.GlobalVariables.*;
import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class GrabberStreamWorker implements Runnable {
    private final AbstractStreamingMediaTask<?> task;
    private final String rotateImage;
    private final FFmpegFrameGrabber grabber;

    long videoFrameCounter = 0;
    long audioFrameCounter = 0;

    long lastFrameReceivedTime = 0;

    final int useFpsDelay;
    final boolean filePlay;

    private final BanalyticsWatermarkPreprocessor banalyticsWatermarkPreprocessor = new BanalyticsWatermarkPreprocessor(null, null);

    public GrabberStreamWorker(AbstractStreamingMediaTask<?> task, FFmpegFrameGrabber grabber, boolean filePlay, int fpsControl, String rotateImage) {
        this.task = task;
        this.grabber = grabber;
        this.rotateImage = rotateImage;
        this.filePlay = filePlay;
        this.useFpsDelay = fpsControl > 0 ? 1000 / fpsControl : 0;
    }

    private final ExecutionContext context = new ExecutionContext();

    int previousFrameWidth = -1;

    double[] realFrameRateMeasurements = new double[10];
    int pos = 0;

    private void initRTFps(double initVal) {
        Arrays.fill(realFrameRateMeasurements, initVal);
    }

    private void pushRTFpsVal(double value) {
        realFrameRateMeasurements[pos] = value;
        pos++;
        if (pos >= realFrameRateMeasurements.length) {
            pos = 0;
        }
    }

    double lastCalculated;
    long counter = 0;

    private double avgFps() {
        double sum = 0;
        for (double realFrameRateMeasurement : realFrameRateMeasurements) {
            sum += realFrameRateMeasurement;
        }
        if (lastCalculated == 0 || counter % realFrameRateMeasurements.length == 0) {
            lastCalculated = sum / realFrameRateMeasurements.length;
        }
        counter++;
        return lastCalculated;
    }

    long measurementStartTime;


    private static FFmpegFrameFilter getRotationFilter(FFmpegFrameGrabber grabber, String rotate) {
        if (rotate == null) {
            return null;
        }
        FFmpegFrameFilter frameFilter = switch (rotate) {
            case "90" -> new FFmpegFrameFilter("transpose=clock", grabber.getImageWidth(), grabber.getImageHeight());
            case "180" -> new FFmpegFrameFilter("rotate=PI", grabber.getImageWidth(), grabber.getImageHeight());
            case "270" -> new FFmpegFrameFilter("transpose=cclock", grabber.getImageWidth(), grabber.getImageHeight());
            default -> null;
        };
        if (frameFilter != null) {
            frameFilter.setPixelFormat(grabber.getPixelFormat());
            frameFilter.setFrameRate(grabber.getFrameRate());
        }
        return frameFilter;
    }

    @Override
    public void run() {
        long frameRateControlTime = 0;
        FFmpegFrameFilter frameFilter = null;
        try {
            int counter = 100;
            while (task.state != RUN) {//10 seconds to get RUN state
                Thread.sleep(100); // wait transition to RUN state
                counter--;
                if (counter == 0) {
                    throw new Exception("Task can't transit to RUN state.");
                }
            }
            log.info("{}: Starting grabber...", task.getUuid());
//            grabber.start();
            grabber.startUnsafe(true);
            double realFrameRate = grabber.getFrameRate();
            initRTFps(realFrameRate);
            if (filePlay) { //file play case
                long sleepTime = (long) (1000 / realFrameRate);
                if (sleepTime == 0) {
                    sleepTime = 100;
                }
                frameRateControlTime = sleepTime;
            }
            frameFilter = getRotationFilter(grabber, rotateImage);
            if (frameFilter != null) {
                frameFilter.start();
            }
        } catch (Throwable e) {
            task.onProcessingException(e);
            return;
        }
        int fpsMeasurementCounter = 0;
        long previousFrameTimestamp = 0;
        TimerTask streamControlJob = createStreamControlJob();
        try {
            SYSTEM_TIMER.schedule(streamControlJob, 10000, 5000);
            log.info("{}: Capture started. Task state: {}", task.getTitle(), task.state);
            while (task.state == RUN) {
                if (fpsMeasurementCounter > 3) {
                    long measurementEndTime = System.currentTimeMillis();
                    double realFrameRate = fpsMeasurementCounter / ((measurementEndTime - measurementStartTime) / 1000.0);
                    pushRTFpsVal(realFrameRate);
                    fpsMeasurementCounter = 0;
                    measurementStartTime = measurementEndTime;
                }
                context.clear();
                if(task instanceof OnvifGrabberTask onvifGrabber){
                    if(onvifGrabber.ptzState!=null) {
                        context.setVar(Onvif.PTZ.class, onvifGrabber.ptzState);
                    }
                }
                if (frameRateControlTime > 0) {//only for file play case
                    Thread.sleep(frameRateControlTime);
                }
                Frame frame;

                frame = grabber.grabFrame();
                if (frame == null) {
                    throw new Exception("Null frame received. Media stream stopped. Restarting task '" + task.getTitle() + "' via " + task.configuration.restartOnFailure);
                }
                frame.timestamp = grabber.getTimestamp();

                if (previousFrameTimestamp == frame.timestamp) {
                    task.sendTaskState("Frozen frame detected");
                }
                previousFrameTimestamp = frame.timestamp;

                if (this.useFpsDelay > 0) {
                    Thread.sleep(this.useFpsDelay);
                }

                int imageWidth = grabber.getImageWidth();
                if (imageWidth == 0) {
                    continue;
                }
                if (previousFrameWidth != -1) {
                    if (imageWidth != previousFrameWidth) {
                        throw new Exception("Frame size changed " + previousFrameWidth + "-> " + imageWidth + " pix. Restarting job: " + task.getTitle());
                    }
                }

                if (getInstance().isShowBanalyticsWatermark()) {
                    banalyticsWatermarkPreprocessor.preProcess(frame);
                }

                previousFrameWidth = imageWidth;
                lastFrameReceivedTime = System.currentTimeMillis();

                boolean videoFrame = frame.getTypes().contains(Frame.Type.VIDEO);
                boolean videoKeyFrame = frame.keyFrame && videoFrame;

                if (videoFrame) {
                    videoFrameCounter++;
                    fpsMeasurementCounter++;

                    if (frameFilter != null) {
                        frameFilter.push(frame);
                        frame = frameFilter.pull();
                    }
                } else {
                    audioFrameCounter++;
                }

                context.setVar(Frame.class, frame);
                context.setVar(FrameGrabber.class, grabber);
                context.setVar(VIDEO_KEY_FRAME, videoKeyFrame);
                context.setVar(SOURCE_TASK_UUID, task.getUuid());
                context.setVar(CALCULATED_FRAME_RATE, avgFps());

                task.onFrameReceived(frame, videoKeyFrame, avgFps());

                task.process(context);
            }
            log.info("{}: Capture stopped.", task.getTitle());
        } catch (Throwable e) {
            log.error("{}: Capture stopped with error: {}", task.getTitle(), e.getMessage());
            task.onProcessingException(e);
        } finally {
            streamControlJob.cancel();
        }
        try {

            grabber.close();
            if (frameFilter != null) {
                frameFilter.close();
            }
            log.info("{}: Grabber stopped.", task.getTitle());
        } catch (Throwable e) {
            log.error("{}: Grabber stopped with error: {}", task.getTitle(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private TimerTask createStreamControlJob() {
        return new TimerTask() {
            long prevVideoFrameCounter = 0;
            long prevAudioFrameCounter = 0;

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (task.state == RUN) {
                    if (now - lastFrameReceivedTime > 15000) {//if no frame grabbed in last 10 seconds, the restart
                        cancel();// self cancel
                        // and fire restart via exception
                        task.onProcessingException(new Exception("Frozen Media Stream. No frames received. Restarting the task: '" + task.getTitle() + "' via " + task.configuration.restartOnFailure));
                    } else {
                        if (
                                (grabber.hasVideo() && videoFrameCounter == prevVideoFrameCounter)
                                        || (grabber.hasAudio() && audioFrameCounter == prevAudioFrameCounter)
                        ) {
                            cancel();// self cancel
                            // and fire restart via exception
                            task.onProcessingException(new Exception("Frozen Media Stream. Restarting the task: '" + task.getTitle() + "' via " + task.configuration.restartOnFailure));
                        }
                    }
                    prevVideoFrameCounter = videoFrameCounter;
                    prevAudioFrameCounter = audioFrameCounter;
                } else {
                    cancel();
                }
            }
        };
    }
}