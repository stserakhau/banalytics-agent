package com.banalytics.box.module.media.task.classification.yolo;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.MotionEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.ImageClassifier.ClassificationResult;
import com.banalytics.box.module.media.task.AbstractStreamingMediaTask;
import com.banalytics.box.module.media.task.motion.detector.MotionDetectionTask;
import com.banalytics.box.module.media.utils.ZonePainter;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.UMat;

import java.util.*;

import static com.banalytics.box.module.ExecutionContext.GlobalVariables.*;
import static com.banalytics.box.module.media.task.motion.detector.MotionDetectionTask.DEFAULT_ALL_ZONES;
import static com.banalytics.box.module.utils.Utils.nodeType;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_core.bitwise_and;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * https://github.com/bytedeco/javacv/blob/master/samples/YOLONet.java
 */
@Slf4j
public class YoloDetectionTask extends AbstractStreamingMediaTask<YoloDetectionConfig> implements PropertyValuesProvider {

    @Override
    public String getTitle() {
        return "Model: " + configuration.imageClassifierThingUuid;
    }

    public YoloDetectionTask(BoxEngine metricDeliveryService, AbstractListOfTask<?> parent) {
        super(metricDeliveryService, parent);
    }

    @Override
    public Object uniqueness() {
        return configuration.imageClassifierThingUuid;
    }

    @Override
    public Map<String, Class<?>> inSpec() {
        return Map.of(Frame.class.getName(), Frame.class);
    }

    OpenCVFrameConverter.ToMat converter;

    private final ZonePainter zonePainter = new ZonePainter();

    private final Set<String> triggeredRegions = new HashSet<>(5);

    private UMat insensitiveMask;

    private final Set<String> targetClasses = new HashSet<>();

    private ImageClassifier<UMat> imageClassifier;

    @Override
    public void doInit() throws Exception {
        if (configuration.imageClassifierThingUuid != null) {
            imageClassifier = engine.getThingAndSubscribe(configuration.imageClassifierThingUuid, this);
        }
        super.doInit();
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

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        targetDetectionResults.clear();

        this.converter = new OpenCVFrameConverter.ToMat();

        reloadConfig();
    }

    @Override
    public void doStop() throws Exception {
        close(insensitiveMask);
        super.doStop();
    }

    @Override
    public void destroy() {
        if (imageClassifier != null) {
            ((Thing<?>) imageClassifier).unSubscribe(this);
        }
    }

    private void close(Pointer mat) {
        if (mat != null) {
            mat.close();
        }
    }

    private boolean inside = false;

    long eventStunTimeout = 0;

    private Set<String> lastTriggeredZones = Set.of();
    private Map<String, Integer> lastTriggeredClassesMap = new HashMap<>();

    private final List<ClassificationResult> targetDetectionResults = new ArrayList<>();

    private long detectionExpirationTimeout;

    @Override
    protected synchronized boolean doProcess(ExecutionContext executionContext) throws Exception {
        this.grabber = executionContext.getVar(FrameGrabber.class);
        Frame frame = executionContext.getVar(Frame.class);
        Boolean motionDetected = executionContext.getVar(VIDEO_MOTION_DETECTED);
        boolean videoKeyFrame = executionContext.getVar(VIDEO_KEY_FRAME, false);
        double frameRate = executionContext.getVar(CALCULATED_FRAME_RATE);
        try {
            if (frame.image != null) {
                Mat streamColorFrame = converter.convert(frame);
                try (UMat colorMat = streamColorFrame.getUMat(ACCESS_READ)) {
                    if (configuration.drawClasses) {
                        drawDetectionResults(colorMat);
                    }
                    onFrameReceived(frame, videoKeyFrame, frameRate);

                    motionDetected = !this.targetDetectionResults.isEmpty();

                    executionContext.setVar(
                            VIDEO_MOTION_DETECTED,
                            motionDetected
                    );


                    if (inside || System.currentTimeMillis() < detectionExpirationTimeout) {//if image still processed by Network return true - continue to execute the tree without calling subtree
                        return true;
                    }

                    inside = true;

                    final UMat clonedColorMat = colorMat.clone();
                    SystemThreadsService.execute(this, () -> {
                        try {
                            List<ClassificationResult> detectionResults;
                            try {
                                if (this.insensitiveMask == null) {//build exclusion mask
                                    if (!zonePainter.insensitiveAreas.isEmpty()) {
                                        this.insensitiveMask = zonePainter.insensitiveMask(clonedColorMat);
                                    }
                                } else {
                                    bitwise_and(clonedColorMat, insensitiveMask, clonedColorMat);
                                }
                                detectionResults = this.imageClassifier.predict(getUuid(), Collections.singletonList(clonedColorMat),
                                        (float) configuration.confidenceThreshold, (float) configuration.nmsThreshold
                                );
                            } finally {
                                clonedColorMat.close();
                            }

                            synchronized (targetDetectionResults) {
                                targetDetectionResults.clear();
                                triggeredRegions.clear();
                                for (ClassificationResult dr : detectionResults) {
                                    if (targetClasses.isEmpty() || targetClasses.contains(dr.className())) {
                                        targetDetectionResults.add(dr);
                                        zonePainter.checkObjectInZones(dr.x(), dr.y(), dr.width(), dr.height(), triggeredRegions);
                                    }
                                }
                            }
                            Map<String, Integer> triggeredClassesMap = new HashMap<>();
                            for (ClassificationResult result : targetDetectionResults) {
                                triggeredClassesMap.computeIfAbsent(result.className(), (k) -> 0);
                                triggeredClassesMap.computeIfPresent(result.className(), (s, val) -> val + 1);
                            }

                            long now = System.currentTimeMillis();
                            detectionExpirationTimeout = now + configuration.detectionDelay;
                            if (!targetDetectionResults.isEmpty()) {// detected
                                MotionEvent evt = null;
                                Set<String> triggeredZones = null;
                                if (zonePainter.hasDetectionAreas()) {//trigger only in zones
                                    if (!triggeredRegions.isEmpty()) {// and triggered in zone
                                        triggeredZones = triggeredRegions;
                                        evt = new MotionEvent(
                                                nodeType(this.getClass()),
                                                this.getUuid(),
                                                getSelfClassName(),
                                                getTitle(),
                                                getSourceThingUuid(),
                                                triggeredRegions.toArray(new String[0]),
                                                triggeredClassesMap.keySet().toArray(new String[0])
                                        );
                                    }
                                } else {// trigger full image
                                    triggeredZones = DEFAULT_ALL_ZONES;
                                    evt = new MotionEvent(
                                            nodeType(this.getClass()),
                                            this.getUuid(),
                                            getSelfClassName(),
                                            getTitle(),
                                            getSourceThingUuid(),
                                            new String[]{"*"},
                                            triggeredClassesMap.keySet().toArray(new String[0])
                                    );
                                }

                                if (evt != null) {

                                    if (now > eventStunTimeout
                                            || lastTriggeredClassesMap.size() != triggeredClassesMap.size()
                                            || !lastTriggeredClassesMap.equals(triggeredClassesMap)
                                            || lastTriggeredZones.size() != triggeredZones.size()
                                            || !lastTriggeredZones.containsAll(triggeredZones)
                                    ) {
                                        lastTriggeredZones = new HashSet<>(triggeredZones);
                                        lastTriggeredClassesMap = triggeredClassesMap;
                                        engine.fireEvent(evt);
                                        eventStunTimeout = now + configuration.eventStunTimeSec * 1000L;
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            log.error(e.getMessage(), e);
                        } finally {
                            inside = false;
                        }
                    });
                }
            }
        } catch (Throwable e) {
            log.error("Error", e);
        }

        return true;
    }

    private void drawDetectionResults(UMat colorMat) {
        synchronized (targetDetectionResults) {
            for (ClassificationResult result : this.targetDetectionResults) {
                rectangle(colorMat,
                        new Point(result.x(), result.y()),
                        new Point(result.x() + result.width(), result.y() + result.height()),
                        Scalar.MAGENTA, 2, LINE_8, 0);
                putText(
                        colorMat,
                        result.className() + ": " + (int) (result.confidence() * 100) + "%",
                        new Point(result.x(), result.y()),
                        FONT_HERSHEY_SIMPLEX, configuration.fontScale,
                        new Scalar(0, 0, 255, 0),
                        2, 0, false
                );
            }
        }
    }

    @Override
    public synchronized void onFrameReceived(Frame frame, boolean videoKeyFrame, double frameRate, Object... args) throws Exception {
        super.onFrameReceived(frame, videoKeyFrame, frameRate, args);
        mediaStreamToClient(frame, frameRate);
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

    private FrameGrabber grabber;

    @Override
    protected FrameGrabber getGrabber() {
        return grabber;
    }

    @Override
    protected int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(MotionEvent.class);
        return events;
    }

    @Override
    public Set<Class<? extends AbstractTask<?>>> shouldAddAfter() {
        return Set.of(MotionDetectionTask.class);
    }
}
