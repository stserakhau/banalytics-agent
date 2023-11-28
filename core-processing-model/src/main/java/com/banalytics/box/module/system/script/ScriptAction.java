package com.banalytics.box.module.system.script;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.service.ScriptExecutionService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ScriptAction extends AbstractAction<ScriptActionConfiguration> {

    private ScriptExecutionService scriptExecutionService;

    public ScriptAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    public void doInit() throws Exception {
        this.scriptExecutionService = engine.getBean(ScriptExecutionService.class);
    }

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        try {
            scriptExecutionService.execute(executionContext.variables(), configuration.script);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.title;
    }

    @Override
    public void doAction(ExecutionContext ctx) throws Exception {
        this.process(ctx);
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
}
