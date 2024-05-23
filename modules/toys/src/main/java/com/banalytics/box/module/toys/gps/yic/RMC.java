package com.banalytics.box.module.toys.gps.yic;

public record RMC(
        Type type,
        String utsPos,
        Status status,
        double latitude,
        NSIndicator nsIndicator,
        double longitude,
        EWIndicator ewIndicator,
        double speedOverGround,
        double courseOver,
        String ground,
        String date,
        EWIndicator magneticVariation
) {

    public enum Type {
        GN
    }
}
