package com.banalytics.box.module.media.task.storage;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.FileCreatedEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.constants.SplitTimeInterval;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.banalytics.box.TimeUtil.fromMillisToServerTz;
import static com.banalytics.box.module.ExecutionContext.GlobalVariables.*;
import static com.banalytics.box.module.utils.Utils.nodeType;

@Slf4j
public final class ContinousVideoRecordingTask extends AbstractTask<ContinousVideoRecordingConfig> implements MediaCaptureCallbackSupport, FileStorageSupport {
    public ContinousVideoRecordingTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(
                FrameGrabber.class.getName(), FrameGrabber.class,
                Frame.class.getName(), Frame.class,
                SOURCE_TASK_UUID.name(), UUID.class
        );
    }

    private FileStorage fileStorage;

    @Override
    public FileStorage fileStorage() {
        return fileStorage;
    }

    private DateTimeFormatter dateTimeFormat;

    @Override
    public Thing<?> getSourceThing() {
        if (parent == null) {
            return null;
        }
        return parent.getSourceThing();
    }

    @Override
    public Object uniqueness() {
        return configuration.storageUuid;
    }

    @Override
    public void doInit() throws Exception {
        if (this.fileStorage != null) {
            ((Thing<?>) this.fileStorage).unSubscribe(this);
        }
        Thing<?> fileStorageThing = engine.getThingAndSubscribe(configuration.storageUuid, this);
        this.fileStorage = (FileStorage) fileStorageThing;
        this.dateTimeFormat = DateTimeFormatter.ofPattern(configuration.pathPattern.format);
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        shutdown = false;
    }

    private String fileName;
    private FFmpegFrameRecorder recorder;
    private long flushTimeout = 0;
    private long recordingStarted;

    private boolean shutdown;

    int width, height;

    @Override
    protected synchronized boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (shutdown) {
            return true;
        }
        long now = System.currentTimeMillis();
        UUID dataSourceUuid = executionContext.getVar(SOURCE_TASK_UUID);
        FrameGrabber frameGrabber = executionContext.getVar(FrameGrabber.class);
        Frame frame = executionContext.getVar(Frame.class);
        boolean videoKeyFrame = executionContext.getVar(VIDEO_KEY_FRAME) == null || (Boolean) executionContext.getVar(VIDEO_KEY_FRAME);
        boolean timeoutTriggered = now > flushTimeout;

        if (recorder != null && timeoutTriggered && videoKeyFrame) {
            commit(now - recordingStarted);
        }

        if (recorder == null) {
            long fileNameTimePart = SplitTimeInterval.ceilTimeout(now, configuration.splitTimeout);
            LocalDateTime fileTime = fromMillisToServerTz(fileNameTimePart);
            this.flushTimeout = SplitTimeInterval.floorTimeout(now, configuration.splitTimeout);

            final MediaFormat mf = MediaFormat.mp4;

            String fileNamePart = dateTimeFormat.format(fileTime);
            this.fileName = '/' + dataSourceUuid.toString() + '/' + fileNamePart + "." + mf.name();

            log.info("Start recording.\nFlush timeout (calculated / floored): {}/{}\nFileName: {}\nRecording length: {} ms",
                    new Date(flushTimeout), new Date(this.flushTimeout),
                    fileNamePart,
                    this.flushTimeout - now
            );
            File file = fileStorage.startOutputTransaction(fileName);

            width = frame.imageWidth;
            height = frame.imageHeight;
            recordingStarted = now;
            this.recorder = createRecorder(frameGrabber, mf, file, getUuid(), executionContext);
//            this.recorder.start();
            this.recorder.startUnsafe();
        }
        try {
            long fts = frame.timestamp;
            long rts = recorder.getTimestamp();
            if (fts < rts) {
                frame.timestamp = rts + 10;
            }
            recorder.record(frame);
        } catch (FFmpegFrameRecorder.Exception e) {
            log.error("Frame skipped: " + e.getMessage());
            commit(now - recordingStarted);
        }

        return true;
    }

    @Override
    public void doStop() throws Exception {
        shutdown = true;
        Thread.sleep(500);// wait flushing frame from main thread
        long now = System.currentTimeMillis();
        commit(now - recordingStarted);
    }

    private synchronized void commit(long duration) throws Exception {
        if (recorder == null) {
            return;
        }
        try {
            this.recorder.stop();
        } finally {
            this.recorder = null;
            String fileName = this.fileName;
            SystemThreadsService.execute(this,() -> {
                try {
                    Thread.sleep(500);// wait to stop recorder
                    fileStorage.commitOutputTransaction(fileName, (pair) -> {
                        FileCreatedEvent evt = new FileCreatedEvent(
                                nodeType(this.getClass()),
                                this.getUuid(),
                                getSelfClassName(),
                                getTitle(),
                                configuration.storageUuid,
                                pair.getKey()
                        );
                        evt.option("width", width);
                        evt.option("height", height);
                        evt.option("duration", (int) duration);
                        engine.fireEvent(evt);
                    });
                    log.info("Recording committed: {}", new Date());
                } catch (Exception e) {
                    log.error("Recording commit failed.", e);
                }
            });
        }
    }

    public FFmpegFrameRecorder createRecorder(FrameGrabber grabber, MediaFormat mediaFormat, File outputFile, UUID uuid, ExecutionContext executionContext) throws Exception {
        final FFmpegFrameRecorder recorder;

        double frameRate = grabber.getFrameRate();
        int videoBitrate = grabber.getVideoBitrate();
        int audioChannels = grabber.getAudioChannels();
        boolean isVideoExists = frameRate > 0 || videoBitrate > 0;
        boolean isAudioExists = audioChannels > 0;

        if (isVideoExists) {
            double useFrameRate;
            switch (configuration.useFrameRate) {
                case GRABBER_FRAME_RATE -> useFrameRate = grabber.getFrameRate();
                case CALCULATED_FRAME_RATE -> useFrameRate = executionContext.getVar(CALCULATED_FRAME_RATE);
                default -> useFrameRate = executionContext.getVar(CALCULATED_FRAME_RATE);
            }
            frameRate = useFrameRate;
        }

        if (isVideoExists && isAudioExists) {
            recorder = new FFmpegFrameRecorder(
                    outputFile,
                    grabber.getImageWidth(), grabber.getImageHeight(),
                    grabber.getAudioChannels()
            );
        } else if (isVideoExists) {
            recorder = new FFmpegFrameRecorder(
                    outputFile, grabber.getImageWidth(), grabber.getImageHeight()
            );
        } else if (isAudioExists) {
            recorder = new FFmpegFrameRecorder(
                    outputFile, audioChannels
            );
        } else {
            throw new Exception("Grabber doesn't provides video or audio data");
        }

        recorder.setFormat(mediaFormat.name());
        if (isVideoExists) {
//            recorder.setInterleaved(true);
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("crf", "28");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(configuration.videoBitRate.bitrate);
            recorder.setAspectRatio(grabber.getAspectRatio());

            recorder.setFrameRate(frameRate);
            if (frameRate == 0) {
                recorder.setGopSize(10);
            } else {
                recorder.setGopSize((int) frameRate);
            }
        }
        if (isAudioExists) {
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setAudioOption("crf", "0");
            recorder.setAudioQuality(0);
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        }

        return recorder;
    }

    @Override
    public void destroy() {
        if (this.fileStorage != null) {
            ((Thing<?>) this.fileStorage).unSubscribe(this);
            log.debug("{}: unsubscribed", getUuid());
        }
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(FileCreatedEvent.class);
        return events;
    }
}
