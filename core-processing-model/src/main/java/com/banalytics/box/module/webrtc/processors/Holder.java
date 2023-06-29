package com.banalytics.box.module.webrtc.processors;

public class Holder<T> {
    public volatile T val;

    public synchronized void set(T val) {
        this.val = val;
    }

    public synchronized T get() throws InterruptedException {
        while (val == null) {
            wait(100);
        }
        return val;
    }
}
