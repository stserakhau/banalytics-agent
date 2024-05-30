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
public class ThingActionAction extends AbstractAction<ThingActionActionConfiguration> {
    public ThingActionAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public Thing<?> targetThing;

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        log.info("Executing: {} over {}", configuration.action, targetThing.getTitle());
        switch (configuration.action) {
            case START -> targetThing.start(configuration.ignoreAutostartState, true);
            case RESTART -> targetThing.restart();
            case STOP -> targetThing.stop();
        }
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.targetThing + ":" + configuration.action;
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
        if (targetThing == null) {
            return "";
        }
        return configuration.action + ": " + targetThing.getTitle();
    }

    @Override
    public Map<String, Object> uiDetails() {
        if (targetThing == null) {
            super.uiDetails();
        }
        return Map.of(
                TARGET_OBJECT_TITLE, targetThing.getTitle(),
                TARGET_OBJECT_CLASS, targetThing.getSelfClassName()
        );
    }

    @Override
    public UUID getSourceThingUuid() {
        if (targetThing == null) {
            return null;
        }
        return targetThing.getUuid();
    }

    @Override
    public void doInit() throws Exception {
        targetThing = engine.getThing(configuration.targetThing);
        if (targetThing == null) {
            throw new Exception("Thing was removed");
        }
    }
}
