package com.banalytics.box.module.media.task.classification.yolo;

/*
MIT License
Copyright (c) 2021 Florian Bruggisser
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/*
YOLONet Example Information
---------------------------
This is a basic implementation of a YOLO object detection network inference example.
It works with most variants of YOLO (YOLOv2, YOLOv2-tiny, YOLOv3, YOLOv3-tiny, YOLOv3-tiny-prn, YOLOv4, YOLOv4-tiny).
YOLO9000 is not support by OpenCV DNN.
To run the example download the following files and place them in the root folder of your project:
    YOLOv4 Configuration: https://raw.githubusercontent.com/AlexeyAB/darknet/master/cfg/yolov4.cfg
    YOLOv4 Weights: https://github.com/AlexeyAB/darknet/releases/download/darknet_yolo_v3_optimal/yolov4.weights
    COCO Names: https://raw.githubusercontent.com/AlexeyAB/darknet/master/data/coco.names
    Dog Demo Image: https://raw.githubusercontent.com/AlexeyAB/darknet/master/data/dog.jpg
For faster inferencing CUDA is highly recommended.
On CPU it is recommended to decrease the width & height of the network or use the tiny variants.
 */

import com.banalytics.box.module.media.ImageClassifier.ClassificationResult;
import com.banalytics.box.service.PreferableBackend;
import com.banalytics.box.service.PreferableTarget;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_text.FloatVector;
import org.bytedeco.opencv.opencv_text.IntVector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_dnn.*;

@Slf4j
public class YOLONet {

    private final Path configPath;
    private final Path weightsPath;

    private final Path configOnnxPath;
    private final Path namesPath;
    private final int width;
    private final int height;

    private final Size size;
    private final Scalar zero;


    private final PreferableBackend preferableBackend;
    private final PreferableTarget preferableTarget;

    /**
     * Creates a new YOLO network.
     *
     * @param configPath  Path to the configuration file.
     * @param weightsPath Path to the weights file.
     * @param namesPath   Path to the names file.
     * @param width       Width of the network as defined in the configuration.
     * @param height      Height of the network as defined in the configuration.
     */
    public YOLONet(Path configPath, Path weightsPath, Path namesPath,
                   PreferableBackend preferableBackend,
                   PreferableTarget preferableTarget,
                   int width, int height) {
        this.configPath = configPath;
        this.weightsPath = weightsPath;
        this.configOnnxPath = null;
        this.namesPath = namesPath;
        this.preferableBackend = preferableBackend;
        this.preferableTarget = preferableTarget;
        this.width = width;
        this.height = height;
        this.size = new Size(width, height);
        this.zero = new Scalar(0.0);
    }

    public YOLONet(Path configOnnxPath, Path namesPath,
                   PreferableBackend preferableBackend,
                   PreferableTarget preferableTarget,
                   int width, int height) {
        this.configPath = null;
        this.weightsPath = null;
        this.configOnnxPath = configOnnxPath;
        this.namesPath = namesPath;
        this.preferableBackend = preferableBackend;
        this.preferableTarget = preferableTarget;
        this.width = width;
        this.height = height;
        this.size = new Size(width, height);
        this.zero = new Scalar(0.0);
    }

    private Net net;

    private StringVector outNames;
    private MatVector outs;

    private List<String> names;

    /**
     * Initialises the network.
     *
     * @return True if the network initialisation was successful.
     */
    public synchronized boolean setup() throws Exception {
        if (configOnnxPath != null) {
            net = readNetFromONNX(configOnnxPath.toAbsolutePath().toString());
        } else {
            net = readNetFromDarknet(
                    configPath.toAbsolutePath().toString(),
                    weightsPath.toAbsolutePath().toString());
        }
        // setup output layers
        outNames = net.getUnconnectedOutLayersNames();
        outs = new MatVector(outNames.size());

        net.setPreferableBackend(preferableBackend.value);
        net.setPreferableTarget(preferableTarget.value);

        names = Files.readAllLines(namesPath);

        return !net.empty();
    }

    public synchronized void stop() {
        if (net != null) {
            net.close();
            outNames.close();
            outs.close();
            names.clear();
        }
    }

    /**
     * Runs the object detection on the frame.
     *
     * @param frame Input frame.
     * @return List of objects that have been detected.
     */
    public synchronized List<ClassificationResult> predict(UMat frame, float confidenceThreshold, float nmsThreshold) {
        try (Mat inputBlob = blobFromImage(frame,
                1 / 255.0,
                this.size,
                this.zero,
                true, false, CV_32F)) {
            net.setInput(inputBlob);
            // run detection
            net.forward(outs, outNames);

            // evaluate result
            return postprocess(frame, outs, confidenceThreshold, nmsThreshold);
        }
    }

    /**
     * Remove the bounding boxes with low confidence using non-maxima suppression
     *
     * @param frame Input frame
     * @param outs  Network outputs
     * @return List of objects
     */
    private List<ClassificationResult> postprocess(UMat frame, MatVector outs, float confidenceThreshold, float nmsThreshold) {
        try (final IntVector classIds = new IntVector();
             final FloatVector confidences = new FloatVector();
             final RectVector boxes = new RectVector()) {

            for (int i = 0; i < outs.size(); ++i) {
                // extract the bounding boxes that have a high enough score
                // and assign their highest confidence class prediction.
                try (Mat result = outs.get(i);
                     FloatIndexer data = result.createIndexer()) {

                    for (int j = 0; j < result.rows(); j++) {
                        // minMaxLoc implemented in java because it is 1D
                        int maxIndex = -1;
                        float maxScore = Float.MIN_VALUE;
                        for (int k = 5; k < result.cols(); k++) {
                            float score = data.get(j, k);
                            if (score > maxScore) {
                                maxScore = score;
                                maxIndex = k - 5;
                            }
                        }

//                        if (maxScore > confidenceThreshold) {
                        int centerX = (int) (data.get(j, 0) * frame.cols());
                        int centerY = (int) (data.get(j, 1) * frame.rows());
                        int width = (int) (data.get(j, 2) * frame.cols());
                        int height = (int) (data.get(j, 3) * frame.rows());
                        int left = centerX - width / 2;
                        int top = centerY - height / 2;

                        classIds.push_back(maxIndex);
                        confidences.push_back(maxScore);

                        boxes.push_back(new Rect(left, top, width, height));
//                        }
                    }
                }
            }

            // remove overlapping bounding boxes with NMS
            try (IntPointer indices = new IntPointer(confidences.size());
                 FloatPointer confidencesPointer = new FloatPointer(confidences.size())) {
                confidencesPointer.put(confidences.get());

                NMSBoxes(boxes, confidencesPointer, confidenceThreshold, nmsThreshold, indices, 1.f, 0);

                // create result list
                List<ClassificationResult> detections = new ArrayList<>();
                for (int i = 0; i < indices.limit(); ++i) {
                    final int idx = indices.get(i);
                    float confidence = confidences.get(idx);
                    try (Rect rect = boxes.get(idx)) {
                        final int clsId = classIds.get(idx);
                        String className = names.get(clsId);
                        detections.add(new ClassificationResult(clsId, className, confidence, rect.x(), rect.y(), rect.width(), rect.height()));
                    }
                }

                return detections;
            }
        }
    }
}
