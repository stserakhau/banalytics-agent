package com.banalytics.box.module;

import java.util.UUID;

public interface StateSupport {
    UUID getUuid();

    State getState();

    String getStateDescription();
}
