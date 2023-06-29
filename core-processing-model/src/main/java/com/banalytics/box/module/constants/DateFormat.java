package com.banalytics.box.module.constants;

public enum DateFormat {
    YYYYMMDD_HHMMSS("yyyy-MM-dd HH:mm:ss"),
    YYMMDD_HHMMSS("yy-MM-dd HH:mm:ss"),

    DDMMYYYY_HHMMSS("dd-MM-yyyy HH:mm:ss"),
    DDMMYY_HHMMSS("dd-MM-yy HH:mm:ss");

    public final String pattern;

    DateFormat(String pattern) {
        this.pattern = pattern;
    }


}
