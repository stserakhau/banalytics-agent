package com.banalytics.box.module.constants;

import com.banalytics.box.api.integration.suc.SynchronizeSoftwareEvent;

public enum SUCUpdateType {
    /**
     * Upgrade process initiates on start application
     */
    ON_START_APPLICATION,
    /**
     * Upgrade process initiated with receiving {@link SynchronizeSoftwareEvent} from Portal
     */
    PORTAL
}
