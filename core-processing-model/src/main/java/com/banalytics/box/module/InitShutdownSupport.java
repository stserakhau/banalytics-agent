package com.banalytics.box.module;

import java.util.UUID;

public interface InitShutdownSupport {
    UUID getUuid();

    String getTitle();

    void init();

    void start(boolean ignoreAutostartProperty, boolean startChildren);

    void stop();

    void restart();

    default void reloadConfig(){
    }
}
