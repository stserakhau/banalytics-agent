package com.banalytics.box.module.constants;

public enum VideoPreBufferTime {
    OFF(-1),
    s1(1000),
    s2(2000),
    s5(5000);

    public final int intervalMillis;

    VideoPreBufferTime(int rate) {
        this.intervalMillis = rate;
    }
}
