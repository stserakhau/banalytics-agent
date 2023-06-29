package com.banalytics.box.service.events;

import org.springframework.context.ApplicationEvent;

public class TaskConfigurationUpdatedEvent extends ApplicationEvent {
    private final String uuid;

    public TaskConfigurationUpdatedEvent(Object source, String uuid) {
        super(source);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
