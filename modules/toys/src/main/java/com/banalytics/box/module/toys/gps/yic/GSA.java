package com.banalytics.box.module.toys.gps.yic;

public record GSA(
        char mode1,
        int mode2,
        int[] sattelitUsed,
        double pdop,
        double hdop,
        double vdop
) {
}
