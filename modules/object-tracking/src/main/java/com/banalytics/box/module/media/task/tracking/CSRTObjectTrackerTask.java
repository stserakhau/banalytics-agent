package com.banalytics.box.module.media.task.tracking;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import org.bytedeco.opencv.opencv_tracking.TrackerCSRT;
import org.bytedeco.opencv.opencv_video.Tracker;

public class CSRTObjectTrackerTask extends AbstractObjectTrackerTask<CSRTObjectTrackerTaskConfig> {
    public CSRTObjectTrackerTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    protected Tracker createTracker() {
        TrackerCSRT tracker = TrackerCSRT.create();//40 ms CPU only

        return tracker;
    }
}
