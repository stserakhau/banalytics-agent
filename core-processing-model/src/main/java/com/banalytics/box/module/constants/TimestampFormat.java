package com.banalytics.box.module.constants;

/**
 * Target of the type is decreasing amount of files per folder
 */
public enum TimestampFormat {
    yyyyMMdd_hhmmss("yyyy-MM-dd/HH-mm-ss"),
    yyyyMMdd_hh_mmss("yyyy-MM-dd/HH/mm-ss"),
    yyyyMM_dd_hhmmss("yyyy-MM/dd/HH-mm-ss"),
    yyyyMM_dd_hh_mmss("yyyy-MM/dd/HH/mm-ss");

    public final String format;

    TimestampFormat(String format) {
        this.format = format;
    }
}
