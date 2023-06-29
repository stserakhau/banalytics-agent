package com.banalytics.box.module.onvif.task;

import com.banalytics.box.module.*;
import com.banalytics.box.module.standard.Onvif;

import java.util.TimerTask;
import java.util.UUID;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

public class GoToPresetAction extends AbstractAction<GoToPresetActionConfiguration> {
    public GoToPresetAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    Onvif onvif;

    IAction returnAction;

    @Override
    protected boolean isFireAction() {
        return true;
    }

    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid + "->" + configuration.presetToken;
    }

    @Override
    public void doInit() throws Exception {
        onvif = engine.getThingAndSubscribe(configuration.getDeviceUuid(), this);
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        if (configuration.returnActionEnabled) {
            returnAction = engine.findTask(configuration.returnActionUuid);
        } else {
            returnAction = null;
        }
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
        if (!onvif.supportsPTZ()) {
            throw new Exception("error.thing.notInitialized");
        }
        onvif.gotoPreset(configuration.presetToken, 1, 1, 1);
        return true;
    }

    private long blindTimeout = 0;

    @Override
    public synchronized void doAction(ExecutionContext ctx) throws Exception {
        long now = System.currentTimeMillis();
        if (now > blindTimeout) {
            blindTimeout = now + configuration.stunTimeoutSec;
            this.process(ctx);
        }
        if (returnAction != null) {
            SYSTEM_TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {

                        returnAction.action(ctx);
                    } catch (Exception e) {
                        onException(e);
                    }
                }
            }, configuration.returnDelaySec * 1000L);
        }
    }

    @Override
    public UUID getSourceThingUuid() {
        if (onvif == null) {
            return null;
        }
        return ((Thing<?>) onvif).getUuid();
    }
}
