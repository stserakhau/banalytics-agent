package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import com.banalytics.box.service.PreferableBackend;
import com.banalytics.box.service.PreferableTarget;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_video.Tracker;
import org.bytedeco.opencv.opencv_video.TrackerDaSiamRPN;

import java.io.File;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class DaSiamRPNObjectTrackerTask extends AbstractObjectTrackerTask<DaSiamRPNObjectTrackerTaskConfig> {
    public DaSiamRPNObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() throws Exception {

        String[] parts = configuration.computationConfig.split(":");
        PreferableBackend preferableBackend = PreferableBackend.valueOf(parts[0]);
        PreferableTarget preferableTarget = PreferableTarget.valueOf(parts[1]);

        File modelPath = engine.getModelPath("dasiamrpn", configuration.subModelName);

        File model = new File(modelPath, "dasiamrpn_model.onnx");
        File cls1 = new File(modelPath, "dasiamrpn_kernel_cls1.onnx");
        File r1 = new File(modelPath, "dasiamrpn_kernel_r1.onnx");

        TrackerDaSiamRPN tracker = TrackerDaSiamRPN.create(
                new TrackerDaSiamRPN.Params()
                        .model(new BytePointer(model.getAbsolutePath()))
                        .kernel_cls1(new BytePointer(cls1.getAbsolutePath()))
                        .kernel_r1(new BytePointer(r1.getAbsolutePath()))
                        .backend(preferableBackend.value)
                        .target(preferableTarget.value)
        );//45 ms !!!BETTER!!!

        return tracker;
    }
}
