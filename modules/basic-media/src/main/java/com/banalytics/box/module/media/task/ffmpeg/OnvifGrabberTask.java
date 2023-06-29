package com.banalytics.box.module.media.task.ffmpeg;

import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.TimerTask;
import java.util.UUID;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

/**
 * There are two timeout type of options for RTSP:
 * <p>
 * ‘-timeout’
 * <p>
 * Set maximum timeout (in seconds) to wait for incoming connections.
 * <p>
 * A value of -1 mean infinite (default). This option implies the
 * ‘rtsp_flags’ set to ‘listen’. ‘reorder_queue_size’
 * <p>
 * Set number of packets to buffer for handling of reordered packets.
 * <p>
 * ‘-stimeout’
 * <p>
 * Set socket TCP I/O timeout in micro seconds.
 * <p>
 * See the RTSP protocol documentation for more info:
 * http://ffmpeg.org/ffmpeg-protocols.html#rtsp
 * <code>
 * MJPEG
 * ffmpeg -re -stream_loop -1 -r 1 -i 'http://@x.x.x.x/onvifsnap' -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 3 -hls_list_size 10 -hls_wrap 0 output.m3u8
 * <p>
 * FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
 * grabber.setOption("re"," ");
 * grabber.setOption("stream_loop","-1");
 * grabber.setOption("r","2");
 * grabber.setOption("c:v", "libx264");
 * grabber.setOption("c:a", "aac");
 * <p>
 * FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output + ".m3u8", 1280, 720, 1);
 * recorder.setOption("r","2");
 * recorder.setOption("c:v", "libx264");
 * recorder.setOption("c:a", "aac");
 * recorder.setOption("f", "hls");
 * recorder.setOption("strict", "-2");
 * recorder.setOption("hls_time", "3");
 * recorder.setOption("hls_list_size", "10");
 * recorder.setOption("hls_wrap", "0");
 * </code>
 * <p>
 * USB cam + mic
 * ffmpeg -f dshow -i video="Logitech HD Webcam C270":audio="Микрофон (HD Webcam C270)" -s 800x600 -acodec aac -ac 2 -ab 32k -ar 8000 -flush_packets 0 out.mp4
 */
@Slf4j
public class OnvifGrabberTask extends AbstractStreamingMediaTask<OnvifGrabberTaskConfiguration> {
    public OnvifGrabberTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public String getTitle() {
        Thing<? extends IUuid> thing = engine.getThing(configuration.getDeviceUuid());
        if (thing == null) {
            return super.getTitle();
        }
        return thing.getTitle() + " / " + configuration.deviceProfile;
    }

    private FFmpegFrameGrabber grabber;

    private Onvif onvif;

    private String targetUrl;

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid + "/" + configuration.deviceProfile + "/" + configuration.fpsControl;
    }

    @Override
    public UUID getSourceThingUuid() {
        Thing<?> t = getSourceThing();
        if (t == null) {
            return null;
        }
        return t.getUuid();
    }

    @Override
    public Thing<?> getSourceThing() {
        return (Thing<?>) onvif;
    }

    @Override
    public void doInit() throws Exception {
        if (onvif != null) {
            ((Thing<?>) onvif).unSubscribe(this);
        }
        onvif = engine.getThingAndSubscribe(configuration.deviceUuid, this);
        if (onvif == null) {
            throw new Exception("thing.error.notFound");
        }
        super.doInit();
    }

    private Thread grabberThread;

    Onvif.PTZ ptzState;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        log.info("{} : Initialization started: {}", getUuid(), configuration);

        if (onvif instanceof Thing t) {
            t.subscribe(this);
            if (t.getState() != State.RUN) {
                throw new Exception("Can't initialize, RTSP " + t.getTitle() + " provider not initialized");
            }
        }

        int tryCnt = 5;
        while (state == State.STOPPED && tryCnt > 0) {
            log.info("{} : Waiting 1 sec stop of the previous thread.", getUuid());
            Thread.sleep(1000);
            tryCnt--;
        }

        this.targetUrl = onvif.streamURI(configuration.deviceProfile);
        Onvif.MediaParams mediaParams = onvif.mediaParams(configuration.deviceProfile);

        grabber = createGrabber(this.targetUrl, mediaParams);
        grabberThread = new Thread(new GrabberStreamWorker(this, grabber, false, configuration.fpsControl, null));
        grabberThread.start();
        log.info("{}: Initialization finished", getUuid());

        if (onvif.supportsPTZ()) {// if supports PTZ try to track position
            TimerTask ptzStateTrackingTask = new TimerTask() {
                @Override
                public void run() {
                    if (state == State.RUN) {
                        try {
                            ptzState = onvif.ptzState();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e); //if method not supported cancel task
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            };
            SYSTEM_TIMER.schedule(ptzStateTrackingTask, 1000, 1000);
        }

        super.doStart(ignoreAutostartProperty, startChildren);
    }

    @Override
    public void doStop() throws Exception {
        if (this.state != State.ERROR) {
            this.state = State.STOPPING;
        }
        if (this.grabberThread != null) {
            while (!grabberThread.isInterrupted()) {
                log.info("Waiting to stop grabber");
                Thread.sleep(1000);
                grabberThread.interrupt();
            }
        }
        super.doStop();
    }

    @Override
    protected FrameGrabber getGrabber() {
        return grabber;
    }

    @Override
    protected int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    private FFmpegFrameGrabber createGrabber(String rtspUri, Onvif.MediaParams mediaParams) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUri);
        String format = "rtsp";//todo <<<< depends on configuration need to implement cases
        grabber.setFormat(format);
        grabber.setOption("rtbufsize", configuration.rtBufferSizeMb + "M");
        grabber.setOption("flags", "flush_packets");
        grabber.setOption("flags", "discardcorrupt");
        grabber.setImageWidth(mediaParams.width());
        grabber.setImageHeight(mediaParams.height());
        grabber.setVideoOption("tune", "zerolatency");
        grabber.setVideoOption("preset", "ultrafast");
        MediaFormat.rtsp.grabberOptions.forEach(grabber::setOption);

        if (configuration.fpsControl > 0) {
            grabber.setFrameRate(configuration.fpsControl);
        }

        return grabber;
    }

    @Override
    public void destroy() {
        if (onvif != null) {
            ((Thing<?>) onvif).unSubscribe(this);
            log.info("{}: unsubscribed", getUuid());
        }
    }

    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
    }
}
