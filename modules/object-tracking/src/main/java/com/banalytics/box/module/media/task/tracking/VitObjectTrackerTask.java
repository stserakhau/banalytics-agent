package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.service.PreferableBackend;
import com.banalytics.box.service.PreferableTarget;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_video.Tracker;
//import org.bytedeco.opencv.opencv_video.TrackerVit;

import java.io.File;

/**
 * TODO from opencv 4.9.0
 */
public class VitObjectTrackerTask /*extends AbstractObjectTrackerTask<VitObjectTrackerTaskConfig>*/ {
//    public VitObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
//        super(engine, parent);
//    }

//    @Override
//    protected Tracker createTracker() throws Exception {
//        String[] parts = configuration.computationConfig.split(":");
//        PreferableBackend preferableBackend = PreferableBackend.valueOf(parts[0]);
//        PreferableTarget preferableTarget = PreferableTarget.valueOf(parts[1]);
//
//        File modelPath = engine.getModelPath("vittrack", configuration.subModelName);
//
//        File model = new File(modelPath, "vittrack.onnx");

//        TrackerVit tracker = TrackerVit.create(
//                new TrackerVit.Params()
//                        .net(new BytePointer(model.getAbsolutePath()))
//                        .meanvalue(new Scalar(0.485, 0.456, 0.406, 0))
//                        .stdvalue(new Scalar(0.229, 0.224, 0.225, 0.0))
//                        .backend(opencv_dnn.DNN_BACKEND_OPENCV)
//                        .target(opencv_dnn.DNN_TARGET_OPENCL)
//        );//50ms
//
//        return tracker;
//    }
}
