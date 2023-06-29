package com.banalytics.box.module.constants;

public enum SampleRate {
    fDisabled(-1),
    f8000(8000),
    f11025(11025),
    f22050(22050),
    f44100(44100);

    public final int rate;

    SampleRate(int rate) {
        this.rate = rate;
    }
}
