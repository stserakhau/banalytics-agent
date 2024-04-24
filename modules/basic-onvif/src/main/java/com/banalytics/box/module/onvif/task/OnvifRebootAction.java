package com.banalytics.box.module.onvif.task;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.*;
import com.banalytics.box.module.onvif.thing.OnvifThing;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@SubItem(of = OnvifThing.class, singleton = true, group = "onvif-actions")
public class OnvifRebootAction extends AbstractAction<OnvifRebootActionConfiguration> {
    public OnvifRebootAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    Onvif onvif;

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid;
    }

    @Override
    public void doInit() throws Exception {
        onvif = engine.getThingAndSubscribe(configuration.getDeviceUuid(), this);
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
    }

    @Override
    public void doStop() throws Exception {
    }

    @Override
    public void destroy() {
        if (onvif != null) {
            ((Thing<?>) onvif).unSubscribe(this);
        }
        onvif = null;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        String rebootMessage = onvif.reboot();
        log.info("Reboot of {} started: {}", configuration.deviceUuid, rebootMessage);
        return true;
    }

    @Override
    public synchronized void doAction(ExecutionContext ctx) throws Exception {
        this.process(ctx);
    }

    @Override
    public UUID getSourceThingUuid() {
        if (onvif == null) {
            return null;
        }
        return ((Thing<?>) onvif).getUuid();
    }
}
