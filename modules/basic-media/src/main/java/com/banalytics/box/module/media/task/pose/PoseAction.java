package com.banalytics.box.module.media.task.pose;

import com.banalytics.box.module.AbstractAction;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.opencv.core.CvType;

import java.util.ArrayList;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.core.CvType.CV_8U;

/**
 * https://github.com/foss-for-synopsys-dwc-arc-processors/synopsys-caffe-models/blob/master/caffe_models/openpose/caffe_model/README.md
 */
public class PoseAction /*extends AbstractAction<PoseActionConfig>*/ {
    /*public PoseAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    Net net;

    @Override
    public void doInit() throws Exception {
        net = opencv_dnn.readNetFromCaffe("e:\\12-openpose\\openpose_pose_coco.prototxt", "e:\\12-openpose\\pose_iter_440000.caffemodel");
        net.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
        net.setPreferableTarget(opencv_dnn.DNN_TARGET_OPENCL);
    }

    @Override
    public void doAction(ExecutionContext ctx) throws Exception {
        process(1);
        process(2);
        process(3);
        process(4);
        process(5);
    }

    private void process(int index) {
        long st = System.currentTimeMillis();
        Mat img = opencv_imgcodecs.imread("e:/12-openpose/" + index + ".png");
        img.convertTo(img, CvType.CV_32F);
        // read the network model
        //Net net = Dnn.readNetFromTensorflow("c:/data/mdl/body/tf_small.pb");


        // send it through the network
        //Mat inputBlob = Dnn.blobFromImage(img, 1.0, new Size(368,368), new Scalar(0, 0, 0), false, false);
        Mat inputBlob = opencv_dnn.blobFromImage(img, 1.0 / 255, new Size(368, 368), new Scalar(0, 0, 0, 0), false, false, CV_32F);
        net.setInput(inputBlob);
        Mat result = net.forward().reshape(1, 19); // 19 body parts
        //Mat result = net.forward().reshape(1,57); // 19 body parts + 2 * 19 PAF maps

        System.out.println(result);

        // get the heatmap locations
        ArrayList<Point> points = new ArrayList();
        for (int i = 0; i < 18; i++) { // skip background
            Mat heatmap = result.row(i).reshape(1, 46); // 46x46
            DoublePointer minVal = new DoublePointer(1);
            DoublePointer maxVal = new DoublePointer(1);
            Point minLoc = new Point();
            Point maxLoc = new Point();
            opencv_core.minMaxLoc(heatmap, minVal, maxVal, minLoc, maxLoc, new Mat());
            if (maxVal.get() > 0.1) {
                points.add(maxLoc);
            } else {
                points.add(new Point());
            }

        }

        // 17 possible limb connections
        int pairs[][] = {
                {1, 2}, {1, 5}, {2, 3},
                {3, 4}, {5, 6}, {6, 7},
                {1, 8}, {8, 9}, {9, 10},
                {1, 11}, {11, 12}, {12, 13},
                {1, 0}, {0, 14},
                {14, 16}, {0, 15}, {15, 17}
        };

        // connect body parts and draw it !
        float SX = (float) (img.cols()) / 46;
        float SY = (float) (img.rows()) / 46;
        for (int n = 0; n < 17; n++) {
            // lookup 2 connected body/hand parts
            int aIndex = pairs[n][0];
            int bIndex = pairs[n][1];
            Point _a = points.get(aIndex);
            Point _b = points.get(bIndex);

            // we did not find enough confidence before
            if (_a.x() <= 0 || _a.y() <= 0 || _b.x() <= 0 || _b.y() <= 0)
                continue;

            // scale to image size
            Point a = new Point((int) (_a.x() * SX), (int) (_a.y() * SY));
            Point b = new Point((int) (_b.x() * SX), (int) (_b.y() * SY));

            opencv_imgproc.line(img, a, b, new Scalar(0, 200, 0, 0));//2

            opencv_imgproc.circle(img, a, 3, new Scalar(0, 0, 200, 0));//-1
            opencv_imgproc.putText(img, n + ": " + aIndex, a, FONT_HERSHEY_SIMPLEX, 0.3, Scalar.RED, 1, 0, false);//2

            opencv_imgproc.circle(img, b, 3, new Scalar(200, 0, 0, 0));//-1
            opencv_imgproc.putText(img, n + ": " + bIndex, b, FONT_HERSHEY_SIMPLEX, 0.3, Scalar.RED, 1, 0, false);//2
        }

        long en = System.currentTimeMillis();

        System.out.println("Processed in: " + (en - st) + " millis");

        opencv_imgcodecs.imwrite("e:\\12-openpose\\" + index + "-pose.png", img);
    }

    @Override
    protected boolean isFireActionEvent() {
        return false;
    }*/
}
