package com.banalytics.box.module.media.task.motion.storage;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.FileCreatedEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.media.task.Utils;
import com.banalytics.box.module.media.task.motion.detector.MotionDetectionTask;
import com.banalytics.box.module.media.task.sound.SoundDetectionTask;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.*;
import static com.banalytics.box.module.constants.VideoPreBufferTime.OFF;
import static com.banalytics.box.module.utils.Utils.nodeType;

@Slf4j
public final class MotionVideoRecordingTask extends AbstractTask<MotionVideoRecordingConfig> implements MediaCaptureCallbackSupport, FileStorageSupport {
    public MotionVideoRecordingTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
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
    private DateTimeFormatter dateTimeFormat;

    private final LinkedList<Frame> preBuffer = new LinkedList<>();

    @Override
    public FileStorage fileStorage() {
        return fileStorage;
    }

    @Override
    public void doInit() throws Exception {
        if (this.fileStorage != null) {
            ((Thing<?>) this.fileStorage).unSubscribe(this);
        }
        Thing<?> fileStorageThing = engine.getThingAndSubscribe(configuration.storageUuid, this);
        this.fileStorage = (FileStorage) fileStorageThing;
    }

    private long noMotionTime;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        clearPreBuffer();

        this.dateTimeFormat = DateTimeFormatter.ofPattern(configuration.pathPattern.format);
        shutdown = false;
        this.noMotionTime = this.configuration.preBufferSeconds.intervalMillis
                + this.configuration.recordingOnMotionDisappearedTimoutMillis;
    }

    private String fileName;
    private String thumbnailFileName;
    private File recordingFile;
    private File thumbnailFile;
    private FFmpegFrameRecorder recorder;
    private long recordingTimeout;
    private long flushTimeout;

    boolean shutdown;

    long motionDetectedTimestamp;
    long lastMotionTimestamp;

    int width;
    int height;

    boolean thumbnailCreated = false;

    @Override
    protected synchronized boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (shutdown) {
            return true;
        }
        long now = System.currentTimeMillis();
        UUID dataSourceUuid = executionContext.getVar(SOURCE_TASK_UUID);
        FrameGrabber frameGrabber = executionContext.getVar(FrameGrabber.class);
        Frame frame = executionContext.getVar(Frame.class);
        Boolean videoMotionDetected = executionContext.getVar(VIDEO_MOTION_DETECTED);
        Boolean audioMotionDetected = executionContext.getVar(AUDIO_MOTION_DETECTED);
        boolean motionDetected = Boolean.TRUE.equals(videoMotionDetected)
                || Boolean.TRUE.equals(audioMotionDetected);

        if (motionDetected) {
            this.recordingTimeout = now + noMotionTime;
        }

        boolean videoKeyFrame = executionContext.getVar(VIDEO_KEY_FRAME) == null || (Boolean) executionContext.getVar(VIDEO_KEY_FRAME);
        boolean timeoutTriggered = now > this.flushTimeout || now >= this.recordingTimeout;

        if (recorder != null && timeoutTriggered && videoKeyFrame) {
            long motionTime = this.lastMotionTimestamp - this.motionDetectedTimestamp;
            long totalTime = now - this.motionDetectedTimestamp;
            commit(motionTime, totalTime);
        }

        if (recorder == null && motionDetected) {
            this.motionDetectedTimestamp = this.lastMotionTimestamp = System.currentTimeMillis();
            LocalDateTime currentTime = TimeUtil.currentTimeInServerTz();

            this.fileName = '/' + dataSourceUuid.toString() + '/' + this.dateTimeFormat.format(currentTime) + "." + MediaFormat.mp4.name();

            this.recordingFile = this.fileStorage.startOutputTransaction(this.fileName);

            width = frame.imageWidth;
            height = frame.imageHeight;

            log.debug("Recording started:\n\tfile: {}", this.fileName);

            this.recorder = createRecorder(frameGrabber, recordingFile, executionContext);
            this.recorder.startUnsafe();
            this.flushTimeout = now + this.configuration.splitTimeout.intervalMillis;
        }

        if (recorder != null && !thumbnailCreated && frame.getTypes().contains(Frame.Type.VIDEO)) {
            thumbnailCreated = true;
            int fNameIndex = this.fileName.lastIndexOf('/');
            String fPath = this.fileName.substring(0, fNameIndex);
            String fName = this.fileName.substring(fNameIndex);
            this.thumbnailFileName = fPath + "/thumbnails" + fName + ".jpg";
            this.thumbnailFile = this.fileStorage.startOutputTransaction(this.thumbnailFileName);
            Utils.saveFrameToFile(frame, this.thumbnailFile);
            log.debug("\tthumb: {}", this.thumbnailFileName);
        }

        boolean isWriting = recorder != null;
        if (isWriting) {
            if (motionDetected) {
                lastMotionTimestamp = System.currentTimeMillis();
            }
            if (!this.preBuffer.isEmpty()) {
                try {//flush buffer
                    for (Frame preBufferedFrame : this.preBuffer) {
                        long fts = preBufferedFrame.timestamp;
                        long rts = recorder.getTimestamp();
                        if (fts < rts) {
                            preBufferedFrame.timestamp = rts + 10;
                        }
                        try {
                            this.recorder.record(preBufferedFrame);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            log.warn("Pre buffered frame skipped", e);
                        }
                    }
                } finally {// and clear
                    clearPreBuffer();
                }
            }
            try {
                long fts = frame.timestamp;
                long rts = recorder.getTimestamp();
                if (fts < rts) {
                    frame.timestamp = rts + 10;
                }
                this.recorder.record(frame);
            } catch (Throwable e) {
                onProcessingException(e);
                long motionTime = this.lastMotionTimestamp - this.motionDetectedTimestamp;
                long totalTime = now - this.motionDetectedTimestamp;
                commit(motionTime, totalTime);
            }
        } else {//if not writing then pre-buffer
            if (this.configuration.preBufferSeconds != OFF) {
                preBufferFrame(frame);
            }
        }

        return true;
    }

    @Override
    public void onException(Throwable e) {
        super.onException(e);
        this.stop();
    }

    @Override
    public void doStop() throws Exception {
        this.shutdown = true;
        long totalTime = System.currentTimeMillis() - this.motionDetectedTimestamp;
        commit(totalTime, totalTime);
        Thread.sleep(500);// wait flushing frame from main thread
        clearPreBuffer();
    }

    private void clearPreBuffer() {
        preBuffer.forEach(Frame::close);
        preBuffer.clear();
    }

    private void commit(long motionTime, long totalTime) throws Exception {
        if (this.recorder == null) {
            return;
        }
        try {
            log.debug("Flushing record");
            this.recorder.stop();
        } finally {
            this.recorder = null;
            thumbnailCreated = false;
            String fileName = this.fileName;
            String thumbnailFileName = this.thumbnailFileName;
            long recordingSize = this.recordingFile.length();
            long minRecordingSize = configuration.minRecordingSizeKb * 1024;
            SystemThreadsService.execute(this, () -> {
                try {
                    log.debug("Motion time(millis)/size(bytes): {}/{}  (min Time filer = {}; min size = {})", motionTime, recordingSize, configuration.minMotionTimeFilterMillis, minRecordingSize);
                    Thread.sleep(1000);// wait to stop recorder
                    if (motionTime < configuration.minMotionTimeFilterMillis || recordingSize < minRecordingSize) {
                        log.debug("Recording skipped");
                        this.fileStorage.rollbackOutputTransaction(fileName, (contextPath) -> {
                            log.debug("Temporary recording file removed");
                        });
                        this.fileStorage.rollbackOutputTransaction(this.thumbnailFileName, (contextPath) -> {
                            log.debug("Temporary recording file thumbnail removed");
                        });
                    } else {
                        this.fileStorage.commitOutputTransaction(fileName, (dataPair) -> {
                            try {
                                if (thumbnailFile != null) {
                                    this.fileStorage.commitOutputTransaction(thumbnailFileName, (dpTh) -> {
                                        FileCreatedEvent evt = new FileCreatedEvent(
                                                nodeType(this.getClass()),
                                                this.getUuid(),
                                                getSelfClassName(),
                                                getTitle(),
                                                configuration.storageUuid,
                                                dataPair.getKey()
                                        );
                                        evt.option("width", width);
                                        evt.option("height", height);
                                        evt.option("duration", (int) totalTime);
                                        engine.fireEvent(evt);
                                        log.debug("Recording committed: {}", new Date());
                                    });
                                }
                            } catch (Throwable e) {
                                log.error("Thumbnail commit failed.", e);
                                onProcessingException(e);
                            }
                        });
                    }
                } catch (Throwable e) {
                    log.error("Recording commit failed.", e);
                    onProcessingException(e);
                }
            });
        }
    }

    private FFmpegFrameRecorder createRecorder(FrameGrabber grabber, File outputFile, ExecutionContext executionContext) throws Exception {
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

        recorder.setFormat(MediaFormat.mp4.name());
        if (isVideoExists) {
//            recorder.setInterleaved(true);
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

    //            recorder.setOption("hwaccel", "videotoolbox");
//            recorder.setVideoCodecName("h264_videotoolbox");
//            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

    private void preBufferFrame(Frame frame) throws Exception {
        this.preBuffer.addLast(frame.clone());

        Frame first = this.preBuffer.getFirst();
        Frame last = this.preBuffer.getLast();
        while (last.timestamp - first.timestamp > configuration.preBufferSeconds.intervalMillis) {
            Frame removed = this.preBuffer.removeFirst();
            removed.close();
            first = this.preBuffer.getFirst();
        }
    }

    @Override
    public void destroy() {
        if (this.fileStorage != null) {
            ((Thing<?>) this.fileStorage).unSubscribe(this);
        }
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(FileCreatedEvent.class);
        return events;
    }

    public Set<Class<? extends AbstractTask<?>>> shouldAddAfter() {
        return Set.of(MotionDetectionTask.class, SoundDetectionTask.class);
    }
}