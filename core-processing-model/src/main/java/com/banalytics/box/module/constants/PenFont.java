package com.banalytics.box.module.constants;

public enum PenFont {
    FONT_HERSHEY_SIMPLEX (0),
    FONT_HERSHEY_PLAIN (1),
    FONT_HERSHEY_DUPLEX (2),
    FONT_HERSHEY_COMPLEX (3),
    FONT_HERSHEY_TRIPLEX (4),
    FONT_HERSHEY_COMPLEX_SMALL (5),
    FONT_HERSHEY_SCRIPT_SIMPLEX (6),
    FONT_HERSHEY_SCRIPT_COMPLEX (7);

    public final int index;

    PenFont(int index) {
        this.index = index;
    }
}
