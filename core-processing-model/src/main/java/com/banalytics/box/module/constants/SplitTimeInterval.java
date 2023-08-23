package com.banalytics.box.module.constants;

import java.util.Date;

public enum SplitTimeInterval {
    s10(10000),
    m1(60000);

    public final int intervalMillis;

    SplitTimeInterval(int rate) {
        this.intervalMillis = rate;
    }

    public static long ceilTimeout(long time, SplitTimeInterval interval) {
        return (time / interval.intervalMillis) * interval.intervalMillis;
    }

    public static long floorTimeout(long time, SplitTimeInterval interval) {
        return (time / interval.intervalMillis + 1) * interval.intervalMillis;
    }

    public static void main(String[] args) {
        Date date = new Date();
        System.out.println(date);
        long time = date.getTime();
        System.out.println(new Date(ceilTimeout(time, m1)));
        System.out.println(new Date(floorTimeout(time, m1)));
    }
}
