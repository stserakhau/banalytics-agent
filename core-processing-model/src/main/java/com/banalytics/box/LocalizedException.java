package com.banalytics.box;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class LocalizedException extends RuntimeException {
    final String i18n;
    final Object[] args;

    public LocalizedException(String i18n, Object... args) {
        super(i18n + " : " + Arrays.toString(args));
        this.i18n = i18n;
        this.args = args;
    }
}
