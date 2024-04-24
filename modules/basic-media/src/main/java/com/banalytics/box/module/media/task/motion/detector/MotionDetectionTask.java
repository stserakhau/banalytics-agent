package com.banalytics.box.module.media.task.motion.detector;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.MotionEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.media.ImageClassifier;
import com.banalytics.box.module.media.ImageClassifier.ClassificationResult;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.media.task.ffmpeg.LocalMediaGrabberTask;
import com.banalytics.box.module.media.task.ffmpeg.SimpleRTSPGrabberTask;
import com.banalytics.box.module.media.task.motion.storage.MotionVideoRecordingTask;
import com.banalytics.box.module.media.utils.ZonePainter;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_video;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_video.BackgroundSubtractor;

import java.util.Arrays;
import java.util.*;
import java.util.stream.Collectors;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.*;
import static com.banalytics.box.module.media.task.motion.detector.MatrixSizeType.zero;
import static com.banalytics.box.module.media.task.motion.detector.MotionDetectionConfig.MotionTriggerMode.MOTION_AND_CLASSIFIER;
import static com.banalytics.box.module.utils.Utils.nodeType;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_core.bitwise_and;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.opencv.core.Core.BORDER_DEFAULT;

@Slf4j
@SubItem(of = {AbstractMediaGrabberTask.class}, group = "media-motion-processing")
public class MotionDetectionTask extends AbstractStreamingMediaTask<MotionDetectionConfig> implements PropertyValuesProvider {
    public MotionDetectionTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(Frame.class.getName(), Frame.class);
    }

    OpenCVFrameConverter.ToMat converter;

    Size blurSize;

    UMat dilateKernel;

    private final ZonePainter zonePainter = new ZonePainter();

    private ImageClassifier<UMat> imageClassifier;

    @Override
    public void doInit() throws Exception {
        if (configuration.motionTriggerMode == MOTION_AND_CLASSIFIER && configuration.imageClassifierThingUuid != null) {
            imageClassifier = engine.getThingAndSubscribe(configuration.imageClassifierThingUuid, this);
        }
        super.doInit();
    }

    boolean insideClassificator;
    long classificationExpirationTimeout;

    long turnOnDelayTimeout;

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        log.info("Initialization started: {}", configuration);
        if (configuration.autoCalibration) {
            configuration.backgroundHistoryDistThreshold = 0;
            configuration.blurSize = MatrixSizeType.s7x7;
            configuration.backgroundHistorySize = 3;
        }
        if (configuration.blurSize != zero) {
            this.blurSize = new Size(configuration.blurSize.width, configuration.blurSize.height);
        } else {
            this.blurSize = null;
        }

        this.backgroundSubtractor = opencv_video.createBackgroundSubtractorMOG2(configuration.backgroundHistorySize, configuration.backgroundHistoryDistThreshold, false);
        this.converter = new OpenCVFrameConverter.ToMat();
        if (configuration.dilateSize != zero) {
            this.dilateKernel = getStructuringElement(MorphType.MORPH_RECT.index, new Size(configuration.dilateSize.width, configuration.dilateSize.height)).getUMat(ACCESS_READ).clone();//todo memory leak
        } else {
            this.dilateKernel = null;
        }

        this.motionStunTimeout = 0;
        this.frameCounter = 0;
        this.insensitiveMask = null;

        this.currentGrayFrame = new UMat();
        this.blurredGrayFrame = new UMat();
        this.dilatedFrame = new UMat();

        this.contours = new MatVector();
        this.hierarchy = new UMat();
        this.fgMask = new UMat();

        insideClassificator = false;
        triggeredRegions.clear();
        classificationResults.clear();

        reloadConfig();

        log.info("Initialization finished");
        super.doStart(ignoreAutostartProperty, startChildren);
        this.turnOnDelayTimeout = System.currentTimeMillis() + configuration.turnOnDelaySec * 1000L;
    }

    public synchronized void reloadConfig() {
        this.zonePainter.clear();
        if (StringUtils.isNotEmpty(configuration.detectionAreas)) {
            zonePainter.init(configuration.detectionAreas);
        }

        targetClasses.clear();
        if (StringUtils.isNotEmpty(configuration.targetClasses)) {
            String[] classes = configuration.targetClasses.replaceAll("[\\[\\]\"]", "").split(",");
            if (classes.length != 1 || !StringUtils.isEmpty(classes[0])) {
                this.targetClasses.addAll(Arrays.asList(classes));
            }
        }
    }


    BackgroundSubtractor backgroundSubtractor;
    UMat fgMask;

    UMat currentGrayFrame;
    UMat blurredGrayFrame;
    UMat dilatedFrame;

    MatVector contours;
    UMat hierarchy;

    private final static Point DILATE_POINT = new Point(-1, -1);
    private final static Scalar DILATE_BORDER_ONE = new Scalar(1);

//    private final List<UMat> detectedObjects = new ArrayList<>(50);

    private final Set<String> triggeredRegions = new HashSet<>(5);

    private final Set<String> targetClasses = new HashSet<>();
    private final List<ClassificationResult> classificationResults = new ArrayList<>(20);

    private FrameGrabber grabber;

    @Override
    protected FrameGrabber getGrabber() {
        return grabber;
    }

    @Override
    protected int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    int frameCounter = 0;

    private UMat insensitiveMask;

    public static final Set<String> DEFAULT_ALL_ZONES = Set.of("*");
    private Set<String> lastTriggeredZones = Set.of();
    private long motionStunTimeout = 0;

    @Override
    public void doStop() throws Exception {
        close(currentGrayFrame);
        close(blurredGrayFrame);
        close(dilatedFrame);

        close(contours);
        close(hierarchy);
        close(fgMask);

        close(blurSize);
        close(dilateKernel);
        close(backgroundSubtractor);
        close(insensitiveMask);

        super.doStop();
    }

    @Override
    public void destroy() {
        if (imageClassifier != null) {
            ((Thing<?>) imageClassifier).unSubscribe(this);
        }
        super.destroy();
    }

    private void close(Pointer mat) {
        if (mat != null) {
            mat.close();
        }
    }

    UMat targetGrayFrameUnManagedRef;

    /**
     * TODO https://github.com/bytedeco/javacpp-presets/issues/644   UMAT CRASH ON JVM GC !!!
     */
    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        boolean targetMotionDetected = false;
        this.grabber = executionContext.getVar(FrameGrabber.class);
        Frame frame = executionContext.getVar(Frame.class);
        try {
            long now = System.currentTimeMillis();
            if (frame != null && frame.image != null) {
                frameCounter++;
//                detectedObjects.clear();
                triggeredRegions.clear();
                boolean backgroundReady = frameCounter > configuration.backgroundHistorySize;
                Mat streamColorFrame = converter.convert(frame);
                try (UMat colorFrame = streamColorFrame.getUMat(ACCESS_READ)) {
                    int timeDivider = (int) Math.floor(grabber.getFrameRate() / configuration.detectionIntensity);
                    if (timeDivider == 0) {
                        timeDivider = 1;
                    }

                    if (!backgroundReady || frameCounter % timeDivider == 0) {
                        contours.clear();
                        cvtColor(colorFrame, currentGrayFrame, COLOR_BGR2GRAY);

                        if (configuration.blurSize != zero) {
                            GaussianBlur(currentGrayFrame, blurredGrayFrame, blurSize, 0);
                            targetGrayFrameUnManagedRef = blurredGrayFrame;
                        } else {
                            targetGrayFrameUnManagedRef = currentGrayFrame;
                        }
                        if (this.insensitiveMask == null) {//build exclusion mask
                            if (!zonePainter.insensitiveAreas.isEmpty()) {
                                this.insensitiveMask = zonePainter.insensitiveMask(targetGrayFrameUnManagedRef);
                            }
                        }
                        if (this.insensitiveMask != null) {
                            bitwise_and(targetGrayFrameUnManagedRef, insensitiveMask, targetGrayFrameUnManagedRef);
                        }

                        backgroundSubtractor.apply(targetGrayFrameUnManagedRef, fgMask);
                        if (!backgroundReady) {//if background not ready accumulate data and skip motion detection
                            return true;
                        }
                        UMat matToContour;
                        if (dilateKernel != null) {
                            dilate(fgMask, dilatedFrame, dilateKernel, DILATE_POINT, 2, BORDER_DEFAULT, DILATE_BORDER_ONE);
                            matToContour = dilatedFrame;
                        } else {
                            matToContour = fgMask;
                        }
                        findContours(matToContour, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
                    }
                    if (contours.size() > 0) {
                        for (int i = 0; i < contours.size(); i++) {// extract detected motion areas
                            Mat contour = contours.get(i);
                            double contourAreaSize = contourArea(contour);
                            boolean triggeredAreaSize = isTriggeredAreaSize(contourAreaSize);
                            try (Rect rect = boundingRect(contours.get(i))) {
                                zonePainter.checkObjectInZones(rect.x(), rect.y(), rect.width(), rect.height(), triggeredRegions);
                                if ((!zonePainter.hasDetectionAreas() || !triggeredRegions.isEmpty()) && triggeredAreaSize) {
                                    targetMotionDetected = true;
//                                if (extractDetectedObjects) {
//                                    UMat detectedObject = colorFrame.apply(rect).clone();//todo memory leak
//                                    detectedObjects.add(detectedObject);
//                                }
                                }
                            }
                        }
                        if (now > classificationExpirationTimeout) {
                            if (configuration.motionTriggerMode == MOTION_AND_CLASSIFIER && imageClassifier != null && targetMotionDetected && !insideClassificator) {
                                insideClassificator = true;
                                final UMat clonedColorMat = colorFrame.clone();
                                try {
                                    SystemThreadsService.execute(this, () -> {
                                        List<ClassificationResult> results;
                                        try {
                                            results = imageClassifier.predict(this.getUuid(), Collections.singletonList(clonedColorMat), (float) configuration.confidenceThreshold, (float) configuration.nmsThreshold);
                                            classificationExpirationTimeout = System.currentTimeMillis() + configuration.classificationDelay;
                                        } catch (Throwable e) {
                                            log.error(e.getMessage(), e);
                                            sendTaskState(e.getMessage());
                                            return;
                                        } finally {
                                            clonedColorMat.close();
                                            insideClassificator = false;
                                        }

                                        synchronized (this.classificationResults) {
                                            this.classificationResults.clear();
                                            triggeredRegions.clear();
                                            for (ClassificationResult dr : results) {
                                                boolean targetClassDetected = targetClasses.isEmpty() || targetClasses.contains(dr.className());
                                                boolean inTargetZone = zonePainter.checkObjectInZones(dr.x(), dr.y(), dr.width(), dr.height(), triggeredRegions);
                                                if (targetClassDetected && inTargetZone) {
                                                    this.classificationResults.add(dr);
                                                }
                                            }
                                        }
                                    });
                                } catch (Throwable e) {
                                    clonedColorMat.close();
                                }
                            } else {
                                if (now > classificationExpirationTimeout + 1000) {
                                    synchronized (this.classificationResults) {
                                        this.classificationResults.clear();
                                    }
                                }
                            }
                        }
                        if (configuration.drawDetections) {
                            if (configuration.drawNoises) {
                                drawContours(streamColorFrame, contours, -1, Scalar.YELLOW);
                            }
                            for (int i = 0; i < contours.size(); i++) {// render marks on the frame
                                Mat contour = contours.get(i);
                                double contourAreaSize = contourArea(contour);
                                try (Rect rect = boundingRect(contour)) {
                                    if (isTriggeredAreaSize(contourAreaSize)) {
                                        rectangle(streamColorFrame, rect, Scalar.RED, 1, LINE_4, 0);
                                        putText(streamColorFrame, "" + contourAreaSize, rect.tl(), FONT_HERSHEY_SIMPLEX, configuration.fontScale, Scalar.RED, 4, 0, false);
                                    }
                                }
                            }
                        }

                        if (configuration.drawClasses) {
                            synchronized (this.classificationResults) {
                                for (ClassificationResult cr : this.classificationResults) {
                                    try (Point pos = new Point(cr.x(), cr.y());) {
                                        rectangle(streamColorFrame, pos, new Point(cr.x() + cr.width(), cr.y() + cr.height()), Scalar.BLUE, 2, LINE_8, 0);
                                        putText(streamColorFrame, cr.className() + ": " + (int) (cr.confidence() * 100) + "%", pos, FONT_HERSHEY_SIMPLEX, configuration.fontScale, Scalar.MAGENTA, 2, 0, false);
                                    }
                                }
                            }
                        }
                    }
                }
                boolean videoKeyFrame = executionContext.getVar(VIDEO_KEY_FRAME, false);
                double frameRate = executionContext.getVar(CALCULATED_FRAME_RATE);
                frameRate = frameRate == 0 ? 10 : frameRate;
                switch (configuration.debug) {
                    case OFF -> onFrameReceived(frame, videoKeyFrame, frameRate);
                    case TARGET_FRAME -> {
                        try (UMat debugImg = targetGrayFrameUnManagedRef.clone(); Mat debugMat = debugImg.getMat(ACCESS_READ); Frame debugFrame = converter.convert(debugMat)) {
                            onFrameReceived(debugFrame, videoKeyFrame, frameRate);
                        }
                    }
                    case BG_SUBSTRACTOR -> {
                        try (UMat debugImg = fgMask.clone(); Mat debugMat = debugImg.getMat(ACCESS_READ); Frame debugFrame = converter.convert(debugMat)) {
                            onFrameReceived(debugFrame, videoKeyFrame, frameRate);
                        }
                    }
                }

                boolean classDetected = !this.classificationResults.isEmpty();
                final boolean resultDetection = switch (configuration.motionTriggerMode) {
                    case MOTION_ONLY -> targetMotionDetected;
                    case MOTION_AND_CLASSIFIER -> targetMotionDetected && classDetected;
                };

                executionContext.setVar(VIDEO_MOTION_DETECTED, resultDetection);
/*                executionContext.setVar(
                        LIST_OF_MAT,
                        detectedObjects
                );*/
                if (resultDetection) {// if motion detected
                    MotionEvent evt = null;
                    Set<String> triggeredZones = null;
                    Set<String> classes;
                    synchronized (this.classificationResults) {
                        classes = classDetected ? this.classificationResults.stream().map(ClassificationResult::className).collect(Collectors.toSet()) : Set.of();
                    }
                    String[] clss = classes.isEmpty() ? new String[]{"*"} : classes.toArray(new String[0]);
                    if (zonePainter.hasDetectionAreas()) {
                        if (!triggeredRegions.isEmpty()) { // and is in triggered areas, then produce event
                            triggeredZones = triggeredRegions;
                            evt = new MotionEvent(nodeType(this.getClass()), this.getUuid(), getSelfClassName(), getTitle(), getSourceThingUuid(), triggeredZones.toArray(new String[0]), clss);
                        }
                    } else { // and triggered areas not configured produce general event
                        triggeredZones = DEFAULT_ALL_ZONES;
                        evt = new MotionEvent(nodeType(this.getClass()), this.getUuid(), getSelfClassName(), getTitle(), getSourceThingUuid(), triggeredZones.toArray(new String[0]), clss);
                    }
                    if (evt != null) {
                        if ((now > motionStunTimeout && now > turnOnDelayTimeout) || lastTriggeredZones.size() != triggeredZones.size() || !lastTriggeredZones.containsAll(triggeredZones)) {
                            lastTriggeredZones = new HashSet<>(triggeredZones);
                            engine.fireEvent(evt);
                            motionStunTimeout = now + configuration.eventStunTimeSec * 1000L;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            onException(e);
        }
        return true;// continue processing without body execution
    }

    @Override
    public void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
    }

    private boolean isTriggeredAreaSize(double areaSize) {
        return areaSize > configuration.triggeredAreaSize;
    }

    @Override
    public Set<String> provideValues(String propertyName) {
        reloadConfig();
        switch (propertyName) {
            case "classes" -> {
                return targetClasses;
            }
            case "triggerAreas" -> {
                return zonePainter.areasNames(ZonePainter.AreaType.trigger);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(MotionEvent.class);
        return events;
    }

    @Override
    public Set<Class<? extends AbstractTask<?>>> shouldAddBefore() {
        return Set.of(MotionVideoRecordingTask.class);
    }
}