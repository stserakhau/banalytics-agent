package com.banalytics.box.module.media.task.ffmpeg;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.State;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.media.thing.LocalMediaThing;
import com.banalytics.box.module.media.thing.LocalMediaThingConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.UUID;

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
 * -vf "rotate=45*(PI/180)"
 * <p>
 * USB cam + mic
 * ffmpeg -f dshow -i video="Logitech HD Webcam C270":audio="Микрофон (HD Webcam C270)" -s 800x600 -acodec aac -ac 2 -ab 32k -ar 8000 -flush_packets 0 out.mp4
 */
@Slf4j
public class LocalMediaGrabberTask extends AbstractStreamingMediaTask<LocalMediaGrabberTaskConfiguration> {
    public LocalMediaGrabberTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    LocalMediaThing localMediaThing;

    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder();

        UUID urlUUID = configuration.localMediaUuid;
        if (urlUUID != null) {
            Thing<?> thing = engine.getThing(urlUUID);
            sb.append(thing.getTitle());
        }
        return sb.toString();
    }

    private FFmpegFrameGrabber grabber;

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
        return localMediaThing;
    }

    @Override
    public Object uniqueness() {
        return configuration.localMediaUuid;
    }

    @Override
    public void doInit() throws Exception {
        if (localMediaThing != null) {
            ((Thing<?>) localMediaThing).unSubscribe(this);
        }

        if (configuration.localMediaUuid != null) {
            localMediaThing = engine.getThingAndSubscribe(configuration.localMediaUuid, this);
        }
    }

    private Thread grabberThread;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        log.info("{} : Initialization started: {}", getUuid(), configuration);

        LocalMediaThingConfig config = localMediaThing.getConfiguration();

        String[] parts = config.resolution.split("/");
        String resolution = parts[0];
        grabber = createGrabber(config.camera, config.fps, resolution, config.microphone, config.sampleRate);

        grabberThread = new Thread(new GrabberStreamWorker(this, grabber, false, 0, configuration.rotateImage));
        grabberThread.start();
        log.info("{}: Initialization finished", getUuid());

        super.doStart(ignoreAutostartProperty, startChildren);
    }

    @Override
    public void doStop() throws Exception {
        if (this.state != State.ERROR && this.state != State.INIT_ERROR) {
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

    private FFmpegFrameGrabber createGrabber(String videoDevice, double fps, String resolution, String audioDevice, int sampleRate) throws Exception {
        FFmpegFrameGrabber grabber;

        if (SystemUtils.IS_OS_WINDOWS) {
            StringBuilder urlBuilder = new StringBuilder();
            if (StringUtils.isNotEmpty(videoDevice)) {
                urlBuilder.append("video=").append(videoDevice);
            }
            if (!urlBuilder.isEmpty()) {
                urlBuilder.append(":");
            }
            if (StringUtils.isNotEmpty(audioDevice)) {
                urlBuilder.append("audio=").append(audioDevice);
            }
            String url = urlBuilder.toString();
            grabber = new FFmpegFrameGrabber(url);
            grabber.setFormat("dshow");
        } else if (SystemUtils.IS_OS_LINUX) {
            grabber = new FFmpegFrameGrabber(videoDevice);
            grabber.setFormat("v4l2");
        } else {
            throw new Exception("Os not supports: " + SystemUtils.OS_NAME);
        }

        if (StringUtils.isNotEmpty(videoDevice)) {
            grabber.setOption("video_size", resolution);
            grabber.setOption("rtbufsize", configuration.rtBufferSizeMb + "M");
            grabber.setOption("flags", "flush_packets");
            grabber.setFrameRate(fps);
            grabber.setVideoOption("preset", "ultrafast");
            grabber.setVideoOption("tune", "zerolatency");
            grabber.setOption("threads", "1");
        }

        if (StringUtils.isNotEmpty(audioDevice)) {
            grabber.setSampleRate(sampleRate);
        }

        return grabber;
    }

    @Override
    public void destroy() {
        if (localMediaThing != null) {
            localMediaThing.unSubscribe(this);
        }
    }

    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
    }

    @Override
    protected boolean isAllKeyFrames() {
        return true;
    }
}
