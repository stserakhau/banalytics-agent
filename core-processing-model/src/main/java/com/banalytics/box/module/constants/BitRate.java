package com.banalytics.box.module.constants;

public enum BitRate {
    k64(64 * 1024),
    k128(128 * 1024),
    k256(256 * 1024),
    k512(512 * 1024),
    k768(768 * 1024),
    k1024(1024 * 1024),
    k1500(1500 * 1024),
    k2000(2000000),
    k3000(3000000),
    k4000(4000000),
    k5000(5000000),
    k6000(6000000),
    k7000(7000000),
    k8000(8000000);

    public final int bitrate;

    BitRate(int bitrate) {
        this.bitrate = bitrate;
    }
}
