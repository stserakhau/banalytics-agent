package com.banalytics.box.module;

import java.util.UUID;

public interface Singleton extends IUuid {
    default void setUuid(UUID uuid) {
    }
}
