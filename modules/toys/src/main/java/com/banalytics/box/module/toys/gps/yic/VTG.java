package com.banalytics.box.module.toys.gps.yic;

public record VTG(
        Type type,
        double course,
        boolean reference,
        String course2,
        char reference2,
        double horizontalSpeed,
        char units, // knots
        double horizontalSpeed2,
        char units2 // km / hour
) {
    enum Type {
        GN
    }
}
