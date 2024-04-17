package com.banalytics.box.module.onvif.task;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GPAxisChangeEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GamePadStateChangedEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.UUID;

import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;
import static java.lang.Math.abs;

@Slf4j
public class PTZContinousAxisAction extends AbstractAction<PTZContinousAxisActionConfiguration> {
    public PTZContinousAxisAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private Onvif onvif;

//    private IAction returnAction;

    @Override
    protected boolean isFireActionEvent() {
        return false;
    }

    @Override
    public String getTitle() {
        return configuration.getTitle();
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

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (!onvif.supportsPTZ()) {
            throw new Exception("error.thing.notInitialized");
        }

        return true;
    }

    boolean rotating;

    private long stopTime = 0;

    private TimerTask stopTimerTask;

    @Override
    public synchronized void doAction(ExecutionContext ctx) throws Exception {
        AbstractEvent event = ctx.getVar(AbstractEvent.class);

        if (event instanceof GamePadStateChangedEvent gpe) {
            if (!gpe.gamepadId.equals(configuration.gamepadId)) {
                return;
            }

            long now = System.currentTimeMillis();
            stopTime = now + configuration.stopTimeout;

            if (stopTimerTask == null) {
                this.process(ctx);
                stopTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();
                        if (now > stopTime) {
                            stopTimerTask = null;
                            rotating = false;
                            onvif.rotateContinuouslyStop();
                            cancel();
                        }
                    }
                };

                SYSTEM_TIMER.schedule(stopTimerTask, 0, 100);
            }


            double xSpeed = gpe.axes[configuration.axisXIndex];
            double ySpeed = gpe.axes[configuration.axisYIndex];
            double zSpeed = gpe.axes[configuration.axisZoomIndex];

            if (abs(xSpeed) < configuration.stopThreshold) {
                xSpeed = 0;
            }
            if (abs(ySpeed) < configuration.stopThreshold) {
                ySpeed = 0;
            }
            if (abs(zSpeed) < configuration.stopThreshold) {
                zSpeed = 0;
            }

            if (xSpeed == 0 && ySpeed == 0 && zSpeed == 0) {
                if (rotating) {
                    onvif.rotateContinuouslyStop();
                }
                rotating = false;
                return;
            }

            onvif.rotateContinuouslyStart(
                    (float) xSpeed * (configuration.reverseX ? -1 : 1),
                    (float) ySpeed * (configuration.reverseY ? -1 : 1),
                    (float) zSpeed * (configuration.reverseZoom ? -1 : 1)
            );
            rotating = true;
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
