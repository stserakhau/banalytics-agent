package com.banalytics.box.module.constants;

public enum LongTimeInterval {

    HALF_HOUR(30 * 60 * 1000L),
    HOUR(60 * 60 * 1000L),
    HOUR_8(2 * 60 * 60 * 1000L),
    DAY(24 * 3600 * 1000L);


    public final long intervalMillis;

    LongTimeInterval(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }
}
