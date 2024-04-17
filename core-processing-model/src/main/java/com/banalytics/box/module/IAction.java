package com.banalytics.box.module;

import java.util.Map;
import java.util.UUID;

public interface IAction {
    final String TARGET_OBJECT_TITLE = "targetObjectTitle";
    final String TARGET_OBJECT_CLASS = "targetObjectClass";
    String MANUAL_RUN = "manual_run";
    String SCHEDULED_RUN = "scheduled_run";

    UUID getUuid();

    String getTitle();

    void action(ExecutionContext ctx) throws Exception;

    default Map<String, Object> uiDetails() {
        return Map.of();
    }
}
