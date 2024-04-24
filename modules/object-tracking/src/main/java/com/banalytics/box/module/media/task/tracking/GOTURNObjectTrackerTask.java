package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_video.Tracker;
import org.bytedeco.opencv.opencv_video.TrackerGOTURN;

import java.io.File;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class GOTURNObjectTrackerTask extends AbstractObjectTrackerTask<GOTURNObjectTrackerTaskConfig> {
    public GOTURNObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() throws Exception {
        File modelPath = engine.getModelPath("goturn", configuration.subModelName);

        File caffemodel = new File(modelPath, "goturn.caffemodel");
        File proto = new File(modelPath, "goturn.prototxt");

        TrackerGOTURN tracker = TrackerGOTURN.create(new TrackerGOTURN.Params()
                .modelTxt(new BytePointer(proto.getAbsolutePath()))
                .modelBin(new BytePointer(caffemodel.getAbsolutePath()))
        );// 70ms CPU only

        return tracker;
    }
}
