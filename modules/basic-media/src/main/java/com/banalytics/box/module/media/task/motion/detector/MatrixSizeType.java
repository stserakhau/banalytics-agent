package com.banalytics.box.module.media.task.motion.detector;

public enum MatrixSizeType {
    zero(0, 0),
    s3x3(3, 3),
    s5x5(5, 5),
    s7x7(7, 7),
    s9x9(9, 9),
    s13x13(13, 13),
    s15x15(15, 15),
    s17x17(17, 17),
    s19x19(19, 19),
    s23x23(23, 23),
    s31x31(31, 31),
    s51x51(51, 51);

    public final int width;
    public final int height;

    MatrixSizeType(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
