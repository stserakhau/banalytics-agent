package com.banalytics.box.module;

import com.banalytics.box.module.constants.RestartOnFailure;

public interface IConfiguration extends IUuid {
    default boolean isAutostart() {
        return true;
    }

    default void setAutostart(boolean value) {
    }

    default RestartOnFailure getRestartOnFailure() {
        return RestartOnFailure.STOP_ON_FAILURE;
    }

    default void validate() throws Exception {
    }
}
