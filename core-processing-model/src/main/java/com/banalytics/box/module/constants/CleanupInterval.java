package com.banalytics.box.module.constants;

public enum CleanupInterval {
    NA(0),
    s30(30000),
    m1(60000),
    m5(300000),
    m10(600000),
    m15(900000),
    m30(1800000),
    h1(3600000);

    public final int intervalMillis;

    CleanupInterval(int rate) {
        this.intervalMillis = rate;
    }
}
