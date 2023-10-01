package com.banalytics.box.module.media.task.ffmpeg;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.State;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.standard.UrlMediaStream;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.net.MalformedURLException;
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
 * <p>
 * USB cam + mic
 * ffmpeg -f dshow -i video="Logitech HD Webcam C270":audio="Микрофон (HD Webcam C270)" -s 800x600 -acodec aac -ac 2 -ab 32k -ar 8000 -flush_packets 0 out.mp4
 */
@Slf4j
public class SimpleRTSPGrabberTask extends AbstractStreamingMediaTask<SimpleRTSPGrabberTaskConfiguration> {
    public SimpleRTSPGrabberTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder();

        UUID urlUUID = configuration.urlUuid;
        if (urlUUID != null) {
            Thing<?> thing = engine.getThing(urlUUID);
            sb.append(thing.getTitle());
        }
        return sb.toString();
    }

    private FFmpegFrameGrabber grabber;

    private UrlMediaStream urlMediaStream;

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
        return (Thing<?>) urlMediaStream;
    }

    @Override
    public Object uniqueness() {
        return configuration.urlUuid + "/" + configuration.fpsControl;
    }

    @Override
    public void doInit() throws Exception {
        if (urlMediaStream != null) {
            ((Thing<?>) urlMediaStream).unSubscribe(this);
        }

        if (configuration.urlUuid != null) {
            urlMediaStream = engine.getThingAndSubscribe(configuration.urlUuid, this);
        }
    }

    private Thread grabberThread;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        log.info("{} : Initialization started: {}", getUuid(), configuration);

        int tryCnt = 5;
        while (state == State.STOPPED && tryCnt > 0) {
            log.info("{} : Waiting 1 sec stop of the previous thread.", getUuid());
            Thread.sleep(1000);
            tryCnt--;
        }

        urlMediaStream = engine.getThingAndSubscribe(configuration.urlUuid, this);
        String url = urlMediaStream.getUrl();
        boolean filePlay = false;
        if (url.startsWith("file:/")) {
            url = url.substring(6);
            filePlay = true;
        }

        grabber = createGrabber(url, urlMediaStream.getStreamFormat());
        grabberThread = new Thread(new GrabberStreamWorker(this, grabber, filePlay, configuration.fpsControl, null));
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

    private FFmpegFrameGrabber createGrabber(String streamUrl, MediaFormat mediaFormat) throws MalformedURLException {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamUrl);
        grabber.setFormat(mediaFormat.name());
        grabber.setOption("rtbufsize", configuration.rtBufferSizeMb + "M");
//        grabber.setOption("flags", "flush_packets");
//        grabber.setOption("flags", "discardcorrupt");
        grabber.setVideoOption("preset", "ultrafast");
        grabber.setVideoOption("tune", "zerolatency");
        grabber.setOption("threads", "1");
        mediaFormat.grabberOptions.forEach(grabber::setOption);

        if (configuration.fpsControl > 0) {
            grabber.setFrameRate(configuration.fpsControl);
        }

        if(configuration.disableAudioRecording) {
            grabber.setMetadata("audio", "disabled");
        }

        return grabber;
    }

    @Override
    public void destroy() {
        if (urlMediaStream != null) {
            ((Thing<?>) urlMediaStream).unSubscribe(this);
        }
    }

    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
    }
}
