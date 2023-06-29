package com.banalytics.box.module.media.task.motion.detector;

public enum MorphType {
    MORPH_RECT(0), MORPH_CROSS(1), MORPH_ELLIPSE(2);

    public final int index;

    MorphType(int index) {
        this.index = index;
    }
}
