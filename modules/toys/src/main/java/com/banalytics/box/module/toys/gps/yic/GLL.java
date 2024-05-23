package com.banalytics.box.module.toys.gps.yic;

public record GLL(
        Type type,
        double latitude,
        NSIndicator nsIndicator,
        double longitude,
        EWIndicator ewIndicator,
        String utcPosition,
        Status status
) {
    enum Type {
        GN
    }
}
