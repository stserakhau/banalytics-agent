package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.media.task.AbstractMediaGrabberTask;
import org.bytedeco.opencv.opencv_tracking.TrackerKCF;
import org.bytedeco.opencv.opencv_video.Tracker;

@SubItem(of = AbstractMediaGrabberTask.class, singleton = true, group = "media-object-tracker")
public class KCFObjectTrackerTask extends AbstractObjectTrackerTask<KCFObjectTrackerTaskConfig> {
    public KCFObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() {
        TrackerKCF tracker = TrackerKCF.create(
                new TrackerKCF.Params()
                        .detect_thresh(0.2f)
                        .compress_feature(true)
                        .compressed_size(2)
                        .resize(true)
        );//15ms CPU only

        return tracker;
    }
}
