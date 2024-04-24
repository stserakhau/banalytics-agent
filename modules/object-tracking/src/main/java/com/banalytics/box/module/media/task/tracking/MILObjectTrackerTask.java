package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import org.bytedeco.opencv.opencv_video.Tracker;
import org.bytedeco.opencv.opencv_video.TrackerMIL;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class MILObjectTrackerTask extends AbstractObjectTrackerTask<MILObjectTrackerTaskConfig> {
    public MILObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() {
        TrackerMIL tracker = TrackerMIL.create(
                new TrackerMIL.Params()
                        .featureSetNumFeatures(250)
                        .samplerInitInRadius(3.0f)
                        .samplerInitMaxNegNum(65)
                        .samplerSearchWinSize(25)
                        .samplerTrackInRadius(4)
                        .samplerTrackMaxNegNum(65)
                        .samplerTrackMaxPosNum(100000)
        );// 60 ms

        return tracker;
    }
}
