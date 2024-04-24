package com.banalytics.box.module.toys.quadrocopter.action;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GamePadStateChangedEvent;
import com.banalytics.box.module.AbstractAction;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.toys.quadrocopter.QuadrocopterThing;
import com.banalytics.box.module.toys.quadrocopter.model.Quadrocopter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static java.lang.Math.abs;

@Slf4j
@SubItem(of = QuadrocopterThing.class, group = "toys")
public class VectorAction extends AbstractAction<VectorActionConfig> {
    private QuadrocopterThing quadrocopterThing;


    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid + configuration.title;
    }

    public VectorAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    @Override
    public void doInit() throws Exception {
        quadrocopterThing = engine.getThingAndSubscribe(configuration.getDeviceUuid(), this);
    }

    @Override
    public void destroy() {
        if (quadrocopterThing != null) {
            quadrocopterThing.unSubscribe(this);
        }
        quadrocopterThing = null;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        return true;
    }

    @Override
    public synchronized void doAction(ExecutionContext ctx) throws Exception {
        AbstractEvent event = ctx.getVar(AbstractEvent.class);

        if (event instanceof GamePadStateChangedEvent gpe) {
            if (!gpe.gamepadId.equals(configuration.gamepadId)) {
                return;
            }
            final Quadrocopter q = quadrocopterThing.getQuadrocopter();
            if (q == null) {
                return;
            }

            if (configuration.axisXIndex > -1) {
                double xSpeed = gpe.axes[configuration.axisXIndex];
                if (abs(xSpeed) < configuration.stopThreshold) {
                    xSpeed = 0;
                }
                q.targetHeading(xSpeed * (configuration.reverseX ? -1 : 1));
            }

            if (configuration.axisYIndex > -1) {
                double ySpeed = gpe.axes[configuration.axisYIndex];
                if (abs(ySpeed) < configuration.stopThreshold) {
                    ySpeed = 0;
                }
                q.targetPitch(ySpeed * (configuration.reverseY ? -1 : 1));
            }

            if (configuration.axisZIndex > -1) {
                double zSpeed = gpe.axes[configuration.axisZIndex];
                if (abs(zSpeed) < configuration.stopThreshold) {
                    zSpeed = 0;
                }
                q.targetRoll(zSpeed * (configuration.reverseZ ? -1 : 1));
            }

            if (configuration.axisPowerIndex > -1) {
                double powerPosition = gpe.axes[configuration.axisPowerIndex];
                if (configuration.reversePower) {
                    if (configuration.negativePowerAllowed) {
                        q.powerPosition(powerPosition);
                    } else {
                        q.powerPosition(1 - (powerPosition + 1) / 2);
                    }
                } else {
                    if (configuration.negativePowerAllowed) {
                        q.powerPosition(-powerPosition);
                    } else {
                        q.powerPosition((powerPosition + 1) / 2);
                    }
                }
            }

            q.flushState();
        }
    }

    @Override
    protected boolean isFireActionEvent() {
        return false;
    }

    @Override
    public UUID getSourceThingUuid() {
        if (quadrocopterThing == null) {
            return null;
        }
        return quadrocopterThing.getUuid();
    }
}
