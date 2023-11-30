package com.banalytics.box.module.onvif.task;

import com.banalytics.box.module.*;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.UUID;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class PTZRotateAction extends AbstractAction<PTZRotateActionConfiguration> {
    public PTZRotateAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private Onvif onvif;

//    private IAction returnAction;

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid + "->" + configuration.title;
    }

    @Override
    public void doInit() throws Exception {
        onvif = engine.getThingAndSubscribe(configuration.getDeviceUuid(), this);
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
//        if (configuration.returnActionEnabled) {
//            returnAction = engine.findTask(configuration.returnActionUuid);
//        } else {
//            returnAction = null;
//        }
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

    boolean rotating;

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (!onvif.supportsPTZ()) {
            throw new Exception("error.thing.notInitialized");
        }

        rotating = true;
        log.info("Start rotating");
        onvif.rotateContinuouslyStart(
                (float) configuration.speedX,
                (float) configuration.speedY,
                (float) configuration.speedZ
        );

        return true;
    }

    private long stopTime = 0;

    private TimerTask stopTimerTask;
    private TimerTask returnActionTask;

    @Override
    public synchronized void doAction(ExecutionContext ctx) throws Exception {
        long now = System.currentTimeMillis();
        stopTime = now + configuration.stopTimeout;

//        cancelReturnAction();

        if (stopTimerTask == null) {
            this.process(ctx);
            stopTimerTask = new TimerTask() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
//                    log.info("Wait: {}", (stopTime - now));
                    if (now > stopTime) {
                        stopTimerTask = null;
                        rotating = false;
                        log.info("Stop rotating");
                        onvif.rotateContinuouslyStop();
                        cancel();

//                        if (configuration.returnActionEnabled) {
//                            scheduleReturnAction(ctx);
//                        }
                    }
                }
            };

            SYSTEM_TIMER.schedule(stopTimerTask, 0, 100);
        }
    }

    private synchronized void cancelReturnAction() {
        if (returnActionTask != null) {
            returnActionTask.cancel();
            returnActionTask = null;
        }
    }

//    private synchronized void scheduleReturnAction(ExecutionContext ctx) {
//        if (returnAction != null) {
//            returnActionTask = new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        log.info("Return started");
//                        returnAction.action(ctx);
//                    } catch (Throwable e) {
//                        onException(e);
//                    }
//                }
//            };
//            SYSTEM_TIMER.schedule(returnActionTask, configuration.returnDelaySec * 1000L);
//        }
//    }

    @Override
    public UUID getSourceThingUuid() {
        if (onvif == null) {
            return null;
        }
        return ((Thing<?>) onvif).getUuid();
    }
}
