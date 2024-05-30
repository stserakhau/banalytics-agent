package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;
import com.banalytics.box.module.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class TimerAction extends AbstractAction<TimerActionConfiguration> {
    public TimerAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public IAction targetAction;

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        SYSTEM_TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ExecutionContext ctx = new ExecutionContext();
                    ctx.setVar(IAction.SCHEDULED_RUN, IAction.SCHEDULED_RUN);
                    targetAction.action(ctx);
                } catch (Exception e) {
                    TimerAction.this.onException(e);
                }
            }
        }, configuration.delayMillis);
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.getTargetAction();
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
        return configuration.title;
    }

    @Override
    public Map<String, Object> uiDetails() {
        if (targetAction == null) {
            super.uiDetails();
        }
        return Map.of(
                TARGET_OBJECT_TITLE, targetAction.getTitle(),
                TARGET_OBJECT_CLASS, targetAction.getClass()
        );
    }

    @Override
    public UUID getSourceThingUuid() {
        if (targetAction == null) {
            return null;
        }
        return targetAction.getUuid();
    }

    @Override
    public void doInit() throws Exception {
        targetAction = engine.findTask(configuration.targetAction);
        if (targetAction == null) {
            throw new Exception("Action was removed.");
        }
    }
}
