package com.banalytics.box.module.toys.gps.yic;

import java.util.List;

public class GSV {
    Type type;

    List<Message> messages;

    public enum Type {
        GL, GA,

        /**
         * Sattelites information
         */
        GP
    }

    record Message(
            int satInView,
            int satId,
            int elevation,
            int azimuth,
            int snr) {

    }
}
