package com.banalytics.box.module.constants;

public enum TimeInterval {
    NA(0),
    s1(1000),
    s5(5000),
    s10(10000),
    s30(30000),
    m1(60000),
    m5(300000),
    m10(600000),
    m15(900000),
    m30(1800000),
    h1(3600000);

    public final int intervalMillis;

    TimeInterval(int rate) {
        this.intervalMillis = rate;
    }
}
