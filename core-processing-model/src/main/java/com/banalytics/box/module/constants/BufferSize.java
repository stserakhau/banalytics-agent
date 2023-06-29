package com.banalytics.box.module.constants;

public enum BufferSize {
    KB_4(4 * 1024),
    KB_16(16 * 1024),
    KB_128(128 * 1024),
    KB_512(512 * 1024),
    KB_1024(1024 * 1024);

    public final int bytes;

    BufferSize(int bytes) {
        this.bytes = bytes;
    }
}
