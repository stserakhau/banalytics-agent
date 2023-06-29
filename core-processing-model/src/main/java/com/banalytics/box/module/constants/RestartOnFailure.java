package com.banalytics.box.module.constants;


public enum RestartOnFailure {
    STOP_ON_FAILURE(0),
    RESTART_IMMEDIATELY(1000),
    RESTART_10_SECONDS(10000),
    RESTART_30_SECONDS(30000),
    RESTART_60_SECONDS(60000);

    public final int restartDelayMillis;

    RestartOnFailure(int restartDelayMillis) {
        this.restartDelayMillis = restartDelayMillis;
    }
}
