package com.banalytics.box.module;

import java.util.UUID;

public interface IAction {
    String MANUAL_RUN = "manual_run";

    UUID getUuid();

    String getTitle();

    void action(ExecutionContext ctx) throws Exception;
}
