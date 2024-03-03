package com.banalytics.box.module.media.task.classification.yolo;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.ImageClassifier;
import com.banalytics.box.module.State;
import com.banalytics.box.service.PreferableBackend;
import com.banalytics.box.service.PreferableTarget;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.UMat;
import org.springframework.core.annotation.Order;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static com.banalytics.box.module.Thing.StarUpOrder.INTEGRATION;

@Slf4j
@Order(INTEGRATION)
public class YoloWorkerThing extends AbstractThing<YoloWorkerThingConfig> implements ImageClassifier<UMat> {

    private Statistics statistics;

    public YoloWorkerThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public Object uniqueness() {
        return configuration.subModelName;
    }

    @Override
    public String getTitle() {
        return configuration.subModelName + ": (" + configuration.workers + ")";
    }

    private final Set<String> supportedClasses = new TreeSet<>(String::compareTo);

    private final List<YOLONet> yoloPool = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void doInit() throws Exception {
    }

    int startedPoolSize;

    @Override
    protected void doStart() throws Exception {
        statistics = new Statistics();
        File modelPath = engine.getModelPath("yolo", configuration.subModelName);
        File nameClassesFile = new File(modelPath, "config.names");
        try (BufferedReader br = new BufferedReader(new FileReader(nameClassesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                supportedClasses.add(line);
            }
        }

        String[] parts = configuration.computationConfig.split(":");
        PreferableBackend preferableBackend = PreferableBackend.valueOf(parts[0]);
        PreferableTarget preferableTarget = PreferableTarget.valueOf(parts[1]);

        //https://pyimagesearch.com/2018/11/12/yolo-object-detection-with-opencv/
        //https://pyimagesearch.com/2017/11/06/deep-learning-opencvs-blobfromimage-works/
        this.startedPoolSize = configuration.workers;

        File modelFile = new File(modelPath, "config.cfg");
        File weightFile = new File(modelPath, "config.weights");
        File onnxModelFile = new File(modelPath, "model.onnx");
        for (int i = 0; i < configuration.workers; i++) {
            YOLONet yolo;
            if (onnxModelFile.exists()) {
                yolo = new YOLONet(
                        onnxModelFile.toPath(),
                        nameClassesFile.toPath(),
                        preferableBackend,
                        preferableTarget,
                        416, 416
                );
            } else {
                yolo = new YOLONet(
                        modelFile.toPath(),
                        weightFile.toPath(),
                        nameClassesFile.toPath(),
                        preferableBackend,
                        preferableTarget,
                        416, 416
                );
            }
            yolo.setup();
            yoloPool.add(yolo);
            log.info("Yolo {} added to poll", i);
        }
    }

    @Override
    protected void doStop() throws Exception {
        while (yoloPool.size() != this.startedPoolSize) {//wait while
            log.info("Stopping: {} / {}", yoloPool.size(), configuration.workers);
            Thread.sleep(500);
        }
        for (int i = 0; i < yoloPool.size(); i++) {
            YOLONet yoloNet = yoloPool.get(i);
            yoloNet.stop();
            log.info("Yolo {} stopped", i);
        }
        yoloPool.clear();
        log.info("Yolo pool cleared");
    }

    private synchronized YOLONet popFromPool() throws Exception {
        while (yoloPool.isEmpty()) {
            wait();
        }
        return yoloPool.remove(0);
    }

    private synchronized void pushToPool(YOLONet net) {
        yoloPool.add(net);
        notifyAll();
    }

    @Override
    public List<ClassificationResult> predict(UUID requestor, List<UMat> images, float confidenceThreshold, float nmsThreshold) throws Exception {
        if (this.state != State.RUN) {
            return List.of();
        }
        YOLONet yolo = null;
        long now = System.currentTimeMillis();
        long startTime = 0;
        try {
            yolo = popFromPool();
            startTime = System.currentTimeMillis();
            statistics.putWaitTime((int) (startTime - now));
//            log.info("Extracted from pool {}. pool size: {}", requestor, yoloPool.size());
            List<ClassificationResult> results = new ArrayList<>(10);
            for (UMat image : images) {
                List<ClassificationResult> result = yolo.predict(image, confidenceThreshold, nmsThreshold);
                results.addAll(result);
            }
            return results;
        } finally {
            long finishTime = System.currentTimeMillis();
            statistics.putProcessingTime((int) (finishTime - startTime));
            pushToPool(yolo);
//            log.info("Returned to pool {}. pool size: {}", requestor, yoloPool.size());
        }
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "readSupportedClasses" -> {
                return supportedClasses;
            }
            case "readStatistics" -> {
                return statistics;
            }
            default -> throw new RuntimeException("Method not supported: " + method);
        }
    }

    @Getter
    public static class Statistics {
        int minWaitTime = Integer.MAX_VALUE;
        int maxWaitTime = Integer.MIN_VALUE;
        int avgWaitTime = 0;

        public void putWaitTime(int waitTime) {
            if (waitTime < minWaitTime) {
                minWaitTime = waitTime;
            } else if (waitTime > maxWaitTime) {
                maxWaitTime = waitTime;
            }
            avgWaitTime = (avgWaitTime + waitTime) >> 1;
        }

        int minProcessingTime = Integer.MAX_VALUE;
        int maxProcessingTime = Integer.MIN_VALUE;
        int avgProcessingTime = 0;
        int processedCount = 0;
        int totalProcessingTime = 0;

        public void putProcessingTime(int processingTime) {
            processedCount++;
            totalProcessingTime += processingTime;
            avgProcessingTime = totalProcessingTime / processedCount;
            if (processingTime < minProcessingTime) {
                minProcessingTime = processingTime;
            } else if (processingTime > maxProcessingTime) {
                maxProcessingTime = processingTime;
            }
        }
    }
}
