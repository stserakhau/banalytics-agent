package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;
import com.banalytics.box.module.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class TaskActionAction extends AbstractAction<TaskActionActionConfiguration> {
    public TaskActionAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public ITask<?> targetTask;

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        log.info("Executing: {} over {}", configuration.action, targetTask.getTitle());
        switch (configuration.action) {
            case START -> targetTask.start(configuration.ignoreAutostartState, true);
            case RESTART -> targetTask.restart();
            case STOP -> targetTask.stop();
        }
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.targetTask + ":" + configuration.action;
    }

    @Override
    public String doAction(ExecutionContext ctx) throws Exception {
        this.process(ctx);

        return null;
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(ActionEvent.class);
        return events;
    }

    @Override
    public String getTitle() {
        if (targetTask == null) {
            return "";
        }
        return configuration.action + ": " + targetTask.getTitle();
    }

    @Override
    public Map<String, Object> uiDetails() {
        if (targetTask == null) {
            super.uiDetails();
        }
        return Map.of(
                TARGET_OBJECT_TITLE, targetTask.getTitle(),
                TARGET_OBJECT_CLASS, targetTask.getSelfClassName()
        );
    }

    @Override
    public UUID getSourceThingUuid() {
        if (targetTask == null) {
            return null;
        }
        return targetTask.getSourceThingUuid();
    }

    @Override
    public void doInit() throws Exception {
        targetTask = engine.findTask(configuration.targetTask);
        if (targetTask == null) {
            throw new Exception("Task was removed");
        }
    }
}
