package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import com.banalytics.box.service.PreferableBackend;
import com.banalytics.box.service.PreferableTarget;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_video.Tracker;
import org.bytedeco.opencv.opencv_video.TrackerNano;

import java.io.File;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class NanoObjectTrackerTask extends AbstractObjectTrackerTask<NanoObjectTrackerTaskConfig> {
    public NanoObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() throws Exception {
        String[] parts = configuration.computationConfig.split(":");
        PreferableBackend preferableBackend = PreferableBackend.valueOf(parts[0]);
        PreferableTarget preferableTarget = PreferableTarget.valueOf(parts[1]);

        File modelPath = engine.getModelPath("nanotrack", configuration.subModelName);

        File backbone = new File(modelPath, "nanotrack_backbone_sim.onnx");
        File head = new File(modelPath, "nanotrack_head_sim.onnx");

        TrackerNano tracker = TrackerNano.create(
                new TrackerNano.Params()
                        .backbone(new BytePointer(backbone.getAbsolutePath()))
                        .neckhead(new BytePointer(head.getAbsolutePath()))
                        .backend(preferableBackend.value)
                        .target(preferableTarget.value)
        );//0.3 sec

        return tracker;
    }
}
