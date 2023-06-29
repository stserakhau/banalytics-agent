package com.banalytics.box.module.constants;

public enum ScaleToSize {
    DontScale(-1,-1),
    I320x200(320,200),
    I640x480(640,480),
    I800x600(800,600),
    I1024x768(1024,768);

    public final int width;
    public final int height;

    ScaleToSize(int width, int height) {
        this.width = width;
        this.height = height;
    }


}
