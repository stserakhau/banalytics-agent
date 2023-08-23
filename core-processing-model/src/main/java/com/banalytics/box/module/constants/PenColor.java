package com.banalytics.box.module.constants;

public enum PenColor {
    WHITE(255, 255, 255, 0),
    BLUE(20, 255, 255, 0),
    GREEN(20, 255, 20, 0),
    YELLOW(255, 255, 0, 0);

    public final int red;
    public final int green;
    public final int blue;
    public final int alpha;

    PenColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
}
