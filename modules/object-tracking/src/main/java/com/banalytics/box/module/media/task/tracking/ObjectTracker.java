package com.banalytics.box.module.media.task.tracking;

public interface ObjectTracker {
    void trackCentroid();

    void cancelTracking();

    void trackRect(int x, int y, int width, int height);
}
