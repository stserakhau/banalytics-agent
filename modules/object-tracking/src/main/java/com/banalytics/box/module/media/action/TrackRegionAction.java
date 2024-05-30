package com.banalytics.box.module.media.action;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.position.RegionSelectedEvent;
import com.banalytics.box.module.AbstractAction;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.media.task.tracking.AbstractObjectTrackerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class TrackRegionAction extends AbstractAction<TrackRegionActionConfiguration> {
    public TrackRegionAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public AbstractObjectTrackerTask<?> objectTrackerTask;

    @Override
    protected boolean isFireActionEvent() {
        return false;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.objectTrackingTask + ":" + configuration.trackCentroid;
    }

    public synchronized String doAction(ExecutionContext ctx) throws Exception {
        AbstractEvent event = ctx.getVar(AbstractEvent.class);
        if (configuration.isTrackCentroid()) {
            objectTrackerTask.trackCentroid();
        } else {
            if (event instanceof RegionSelectedEvent rse) {
                objectTrackerTask.trackRect(
                        rse.x,
                        rse.y,
                        rse.width,
                        rse.height
                );
            }
        }

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
        if (objectTrackerTask == null) {
            return "";
        }
        return objectTrackerTask.getTitle();
    }

    @Override
    public Map<String, Object> uiDetails() {
        if (objectTrackerTask == null) {
            super.uiDetails();
        }
        return Map.of(
                TARGET_OBJECT_TITLE, objectTrackerTask.getTitle(),
                TARGET_OBJECT_CLASS, objectTrackerTask.getSelfClassName()
        );
    }

    @Override
    public UUID getSourceThingUuid() {
        if (objectTrackerTask == null) {
            return null;
        }
        return objectTrackerTask.getSourceThingUuid();
    }

    @Override
    public void doInit() throws Exception {
        objectTrackerTask = engine.findTask(configuration.objectTrackingTask);
        if (objectTrackerTask == null) {
            throw new Exception("Task was removed");
        }
    }
}
