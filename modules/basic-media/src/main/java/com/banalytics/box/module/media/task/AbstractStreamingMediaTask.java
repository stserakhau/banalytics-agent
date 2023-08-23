package com.banalytics.box.module.media.task;

import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationThing;
import com.banalytics.box.service.SystemThreadsService;
import com.banalytics.box.service.utility.TrafficControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.banalytics.box.module.MediaConsumer.MediaData.MediaType.AUDIO;
import static com.banalytics.box.module.MediaConsumer.MediaData.MediaType.VIDEO;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;


@Slf4j
public abstract class AbstractStreamingMediaTask<CONFIGURATION extends AbstractConfiguration> extends AbstractListOfTask<CONFIGURATION> implements MediaCaptureCallbackSupport {
    static {
        try {
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private Collection<PreProcessor<Frame>> framePreProcessors = new ArrayList<>();

    private PortalWebRTCIntegrationThing portalWebRTCIntegrationThing;

    public AbstractStreamingMediaTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public Map<String, Class<?>> inSpec() {
        return null;
    }

    protected abstract FrameGrabber getGrabber();

    protected abstract int getAudioChannels();

    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        for (PreProcessor<Frame> framePreProcessor : framePreProcessors) {
            framePreProcessor.preProcess(frame);
        }
    }

    @Override
    public void addSubTask(AbstractTask<?> task) {
        if (task instanceof PreProcessor pp) {
            Collection<PreProcessor<Frame>> newArr = new ArrayList<>(this.framePreProcessors);
            newArr.add(pp);
            this.framePreProcessors = newArr;
        }
        super.addSubTask(task);
    }

    @Override
    public void addSubTaskFirst(AbstractTask<?> task) {
        if (task instanceof PreProcessor pp) {
            Collection<PreProcessor<Frame>> newArr = new ArrayList<>(this.framePreProcessors);
            newArr.add(pp);
            this.framePreProcessors = newArr;
        }
        super.addSubTaskFirst(task);
    }

    @Override
    public void addSubTaskWithRule(AbstractTask<?> task) {
        if (task instanceof PreProcessor pp) {
            Collection<PreProcessor<Frame>> newArr = new ArrayList<>(this.framePreProcessors);
            newArr.add(pp);
            this.framePreProcessors = newArr;
        }
        super.addSubTaskWithRule(task);
    }

    @Override
    public void removeSubTask(AbstractTask<?> task) {
        if (task instanceof PreProcessor<?> pp) {
            Collection<PreProcessor<Frame>> newArr = new ArrayList<>(this.framePreProcessors);
            newArr.remove(pp);
            this.framePreProcessors = newArr;
        }
        super.removeSubTask(task);
    }

    double aspectRatio = -1;

    private TimerTask qualityControl;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        //engine is null when blankOf executed
        this.portalWebRTCIntegrationThing = engine.getThing(PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID);
        aspectRatio = -1;
        super.doStart(ignoreAutostartProperty, startChildren);
        initQualityControl();
    }

    @Override
    public void doStop() throws Exception {
        stopQualityControl();
        super.doStop();
    }

    private void initQualityControl() {
        int period = 3000;
        this.qualityControl = new TimerTask() {
            @Override
            public void run() {
                if(portalWebRTCIntegrationThing.getConfiguration().adaptiveBitrate) {
                    int ttlfCnt = totalVideoFrameCounter.get();
                    int skfCnt = skippedVideoFrameCounter.get();
                    totalVideoFrameCounter.set(0);
                    skippedVideoFrameCounter.set(0);
                    if (ttlfCnt == 0) {
                        return;
                    }
                    double skf = skfCnt / (double) ttlfCnt;
                    double load = TrafficControl.INSTANCE.bandwidthLoad();
                    log.info("Bandwidth load: {} Skipped frames: {}/{} ({})", load, skfCnt, ttlfCnt, skf);
                    if (skf > 0.3 && load > 1) {
                        increaseQuality = -2;
                    } else if (skf > 0.2 || load > 0.8) {
                        increaseQuality = -1;
                    } else if (load < 0.7) {
                        increaseQuality = 1;
                    } else {
                        increaseQuality = 0;
                    }
                } else {
                    increaseQuality = 1;
                }
            }
        };
        SYSTEM_TIMER.schedule(qualityControl, period, period);
    }

    private void stopQualityControl() {
        if (qualityControl != null) {
            qualityControl.cancel();
            qualityControl = null;
        }
    }

    private List<Consumer<MediaResult>> consumerList = new ArrayList<>();

    public void screenShot(Consumer<MediaResult> mediaResultConsumer) {
        consumerList.add(mediaResultConsumer);
    }

    private void sendScreen(List<Consumer<MediaResult>> consumerList, Frame frame) {
        try {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage img = converter.convert(frame);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 1024);
            ImageIO.write(img, "jpg", baos);

            MediaResult mediaResult = new MediaResult(getUuid(), MediaResult.MediaType.image, baos.toByteArray());
            mediaResult.width = frame.imageWidth;
            mediaResult.height = frame.imageHeight;
            for (Consumer<MediaResult> mediaResultConsumer : consumerList) {
                mediaResultConsumer.accept(mediaResult);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private volatile boolean sendingFrame = false;

    private int increaseQuality = 0;

    private final AtomicInteger keyFrameCounter = new AtomicInteger(0);
    private final AtomicInteger totalVideoFrameCounter = new AtomicInteger(0);
    private final AtomicInteger skippedVideoFrameCounter = new AtomicInteger(0);

    protected boolean isAllKeyFrames() {
        return false;
    }

    protected synchronized void mediaStreamToClient(Frame sourceFrame, double frameRate) throws Exception {
        if (consumerVideoStreamMap.isEmpty() && consumerList.isEmpty()) {
            return;
        }
        boolean isVideo = sourceFrame.getTypes().contains(Frame.Type.VIDEO);
        boolean isAudio = sourceFrame.getTypes().contains(Frame.Type.AUDIO);
        if (!isVideo && !isAudio) {
            return;
        }
        // make camera shot if requested
        if (!consumerList.isEmpty() && isVideo) {
            List<Consumer<MediaResult>> localConsumerList = this.consumerList;
            this.consumerList = new ArrayList<>();
            final Frame screenShot = sourceFrame.clone();
            SystemThreadsService.execute(this, () -> {
                sendScreen(localConsumerList, screenShot);
                screenShot.close();
            });
        }
        if (!consumerVideoStreamMap.isEmpty()) {
            totalVideoFrameCounter.incrementAndGet();
            int frameNum = 0;
            if (isVideo) {
                frameNum = keyFrameCounter.incrementAndGet();
            }

            if (aspectRatio == -1) {
                FrameGrabber grabber = getGrabber();
                aspectRatio = grabber.getAspectRatio();
            }
            boolean synchronouseMediaWrite = portalWebRTCIntegrationThing.getConfiguration().synchronousMediaStream;
            boolean isSyncWrite = synchronouseMediaWrite || isAudio || sourceFrame.keyFrame && (!isAllKeyFrames() || frameNum % 30 == 0);
            if (isSyncWrite) {
                recordFrame(sourceFrame, frameRate);
            } else {
                if (sendingFrame) {
                    skippedVideoFrameCounter.incrementAndGet();
                    return;
                }
                sendingFrame = true;
                final Frame frame = sourceFrame.clone();
                SystemThreadsService.execute(this, () -> {
                    try (frame) {
                        recordFrame(frame, frameRate);
                    } catch (Throwable e) {
                        onException(e);
                    } finally {
                        sendingFrame = false;
                    }
                });
            }
        }
    }

    private synchronized void recordFrame(Frame frame, double frameRate) throws Exception {
        if (increaseQuality < 0) {
            for (RealTimeOutputStream rts : consumerVideoStreamMap.values()) {
                rts.decreaseQuality(increaseQuality < -1);
            }
        }
        if (frame.getTypes().contains(Frame.Type.AUDIO)) {
            for (RealTimeOutputStream stream : consumerAudioStreamMap.values()) {
                if (stream.hasConsumers()) {
                    FFmpegFrameRecorder rtAudioRecorder = audioStreamRecorderMap.get(stream);
                    if (rtAudioRecorder == null) {
                        rtAudioRecorder = createRealTimeAudioRecorder(stream);
                        rtAudioRecorder.start();
                        audioStreamRecorderMap.put(stream, rtAudioRecorder);
                    }

                    long fts = frame.timestamp;
                    long rts = rtAudioRecorder.getTimestamp();
                    if (fts < rts) {
                        frame.timestamp = rts + 10;
                    }

                    try {
                        log.info("Write Audio frame");
                        rtAudioRecorder.recordSamples(frame.sampleRate, frame.audioChannels, frame.samples);
                    } catch (FFmpegFrameRecorder.Exception e) {
                        log.error(e.getMessage(), e);
                        freeRTAudioRecorder(stream);
                    }
                } else {
                    freeRTAudioRecorder(stream);
                }
            }
        }
        if (frame.getTypes().contains(Frame.Type.VIDEO)) {
            for (RealTimeOutputStream stream : consumerVideoStreamMap.values()) {
                if (increaseQuality > 0) {
                    stream.increaseQuality();
                }
                if (stream.hasConsumers()) {
                    RecorderWrapper rtVideoRecorderWrapper = videoStreamRecorderMap.get(stream);

                    stream.setStreamSize(frame.imageWidth, frame.imageHeight);
                    stream.setFps(frameRate);
                    {//video stream part
                        double fpsDeviation = 1;
                        if (rtVideoRecorderWrapper != null) {
                            fpsDeviation = Math.min(rtVideoRecorderWrapper.fps, frameRate) / Math.max(rtVideoRecorderWrapper.fps, frameRate);
                        }

                        if (stream.propsChanged || stream.bitrateChanged || fpsDeviation < 0.98) {
                            stream.propsChanged = false;
                            stream.bitrateChanged = false;
                            //                    log.info("Stream properties changed");
                            freeRTVideoRecorder(stream);
                            rtVideoRecorderWrapper = null;
                        }
                        if (rtVideoRecorderWrapper == null) {
//                                    System.out.println("========>>>>>>>>>> stream bitrate " + stream.bitrate());
                            FFmpegFrameRecorder rtVideoRecorder = createRealTimeVideoStreamRecorder(stream.bitrate(), frameRate, stream.currentWidth, stream.currentHeight, aspectRatio, stream);
                            rtVideoRecorderWrapper = new RecorderWrapper(frameRate, rtVideoRecorder);
                            //                    log.info("Recorder created ({}): {}x{} / {} / {}", realTimeRecorder, stream.currentWidth, stream.currentHeight, frameRate, stream.bitrate());
                            rtVideoRecorder.start();
                            //                    realTimeRecorder.startUnsafe();
                            //                    log.info("Recorder started");
                            videoStreamRecorderMap.put(stream, rtVideoRecorderWrapper);
                        }
                    }
                    long fts = frame.timestamp;
                    long rts = rtVideoRecorderWrapper.recorder.getTimestamp();
                    if (fts < rts) {
                        frame.timestamp = rts + 10;
                    }

                    try {
                        rtVideoRecorderWrapper.recorder.record(frame);
                    } catch (FFmpegFrameRecorder.Exception e) {
                        log.error("Writing frame failed: {}", e.getMessage());
                        freeRTVideoRecorder(stream);
                    }
                } else {
                    freeRTVideoRecorder(stream);
                }
            }
        }

    }

    private void freeRTVideoRecorder(RealTimeOutputStream stream) throws Exception {
        RecorderWrapper realTimeRecorder = videoStreamRecorderMap.remove(stream);
        if (realTimeRecorder != null) {
//            log.info("Recorder stopped: {}", realTimeRecorder);
            realTimeRecorder.recorder.stop();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //todo skip interruption exception
            } finally {
                stream.flush();
            }
        }
    }

    private void freeRTAudioRecorder(RealTimeOutputStream stream) throws Exception {
        FFmpegFrameRecorder realTimeRecorder = audioStreamRecorderMap.remove(stream);
        if (realTimeRecorder != null) {
//            log.info("Recorder stopped: {}", realTimeRecorder);
            realTimeRecorder.stop();
            Thread.sleep(50);
            stream.flush();
        }
    }

    private final Map<MediaConsumer, RealTimeOutputStream> consumerVideoStreamMap = new ConcurrentHashMap<>();
    private final Map<RealTimeOutputStream, RecorderWrapper> videoStreamRecorderMap = new ConcurrentHashMap<>();

    private final Map<MediaConsumer, RealTimeOutputStream> consumerAudioStreamMap = new ConcurrentHashMap<>();
    private final Map<RealTimeOutputStream, FFmpegFrameRecorder> audioStreamRecorderMap = new ConcurrentHashMap<>();

    public RealTimeOutputStream createRealTimeVideoStream(int streamId, MediaConsumer consumer) {
        if (state != State.RUN) {
            return null;
        }
        RealTimeOutputStream stream = consumerVideoStreamMap.computeIfAbsent(consumer, c -> {
            log.info("RT Video Stream created");
            return new RealTimeOutputStream(this.portalWebRTCIntegrationThing.getConfiguration(), streamId, VIDEO);
        });
        log.info("RT Video Stream consumer added");
        stream.addPacketConsumer(consumer);
        return stream;
    }

    public RealTimeOutputStream createRealTimeAudioStream(int streamId, MediaConsumer consumer) {
        if (state != State.RUN) {
            return null;
        }
        RealTimeOutputStream stream = consumerAudioStreamMap.computeIfAbsent(consumer, c -> {
            log.info("RT Audio Stream created");
            return new RealTimeOutputStream(this.portalWebRTCIntegrationThing.getConfiguration(), streamId, AUDIO);
        });
        log.info("RT Audio Stream consumer added");
        stream.addPacketConsumer(consumer);
        return stream;
    }

    @Override
    public RealTimeOutputStream getRealTimeVideoStream(int streamId, MediaConsumer consumer) {
        return consumerVideoStreamMap.get(consumer);
    }

    @Override
    public RealTimeOutputStream getRealTimeAudioStream(int streamId, MediaConsumer consumer) {
        return consumerAudioStreamMap.get(consumer);
    }

    @Override
    public void releaseRealTimeVideoStream(int streamId, MediaConsumer consumer) throws FFmpegFrameRecorder.Exception {
        log.info("RT Video stream destroying");
        RealTimeOutputStream rtStream = consumerVideoStreamMap.get(consumer);
        if (rtStream == null) {
            return;
        }
        rtStream.removePacketConsumer(consumer);
        if (!rtStream.hasConsumers()) {
            consumerVideoStreamMap.remove(consumer);
            SystemThreadsService.execute(this, () -> {
                RecorderWrapper recorder = videoStreamRecorderMap.remove(rtStream);
                if (recorder != null) {
                    try {
                        recorder.recorder.stop();
                    } catch (FFmpegFrameRecorder.Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
        log.info("RT Video stream destroyed");
    }

    @Override
    public void releaseRealTimeAudioStream(int streamId, MediaConsumer consumer) throws FFmpegFrameRecorder.Exception {
        log.info("RT Audio stream destroying");
        RealTimeOutputStream rtStream = consumerAudioStreamMap.get(consumer);
        if (rtStream == null) {
            return;
        }
        rtStream.removePacketConsumer(consumer);
        if (!rtStream.hasConsumers()) {
            consumerAudioStreamMap.remove(consumer);
            SystemThreadsService.execute(this, () -> {
                FFmpegFrameRecorder recorder = audioStreamRecorderMap.remove(rtStream);
                if (recorder != null) {
                    try {
                        recorder.stop();
                    } catch (FFmpegFrameRecorder.Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
        log.info("RT Audio stream destroyed");
    }

    private FFmpegFrameRecorder createRealTimeVideoStreamRecorder(int bitRate, double frameRate, int imageWidth, int imageHeight, double aspectRatio, OutputStream outputStream) {
        PortalWebRTCIntegrationConfiguration config = portalWebRTCIntegrationThing.getConfiguration();
        PortalWebRTCIntegrationConfiguration.QualityProfile qp = config.rtMediaQualityProfile;
        final FFmpegFrameRecorder recorder;

        double k = (double) imageHeight / imageWidth;

        int targetWidth = Math.min(imageWidth, 1024);
        int targetHeight = (int) (targetWidth * k);

        recorder = new FFmpegFrameRecorder(outputStream, targetWidth, targetHeight);

        recorder.setFormat(MediaFormat.h264.name());
        recorder.setInterleaved(false);

//        recorder.setVideoOption("tune", "zerolatency");
//        recorder.setVideoOption("preset", "ultrafast");
//        recorder.setVideoOption("crf", "26");

//        recorder.setVideoOption("tune", qp.tune);
//        recorder.setVideoOption("preset", qp.preset);
//        recorder.setVideoOption("crf", "" + qp.crf);

//        recorder.setVideoOption("tune", "film");
//        recorder.setVideoOption("preset", "slower");
//        recorder.setVideoOption("crf", "10");

        recorder.setVideoOption("threads", "1");//https://superuser.com/questions/155305/how-many-threads-does-ffmpeg-use-by-default
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoBitrate(bitRate);
        recorder.setAspectRatio(aspectRatio);

        int gop = (int) (frameRate * qp.gopSeconds);
        if (gop < 1) {
            gop = 1;
        }
        recorder.setGopSize(gop);
        log.debug("RT Video Stream grabber created");
        return recorder;
    }

    int counter = 0;

    private FFmpegFrameRecorder createRealTimeAudioRecorder(OutputStream outputStream) {
        final FFmpegFrameRecorder recorder;
        counter++;
        recorder = new FFmpegFrameRecorder(outputStream, 1);

        recorder.setFormat("wav");
        recorder.setSampleFormat(AV_SAMPLE_FMT_S16);
        recorder.setSampleRate(16000);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);
//        recorder.setOption("fragment_size", "512");
//        recorder.setOption("re", "");
//        recorder.setOption("fflags", "nobuffer");
//        recorder.setOption("avioflags", "direct");
//        recorder.setOption("probesize", "32");
//        recorder.setOption("rtbufsize", "2048");
//        recorder.setAudioOption("buffer_size", "2048");
//        recorder.setOption("fflags", "discardcorrupt");
//        recorder.setOption("flags", "low_delay");
//        recorder.setOption("framedrop", "1");
//        recorder.setOption("strict", "experimental");
//        recorder.setOption("analyzeduration", "0");
//        recorder.setOption("sync", "ext");


        log.info("RT Audio Stream grabber created");
        return recorder;
    }

    @RequiredArgsConstructor
    private static class RecorderWrapper {
        final double fps;
        final FFmpegFrameRecorder recorder;
    }
}
