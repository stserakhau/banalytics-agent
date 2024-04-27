package com.banalytics.box.module.toys.quadrocopter.action;

import com.banalytics.box.api.integration.model.SubItem;
import com.banalytics.box.module.AbstractAction;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.toys.quadrocopter.QuadrocopterThing;
import com.banalytics.box.module.toys.quadrocopter.model.Quadrocopter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@SubItem(of = QuadrocopterThing.class, group = "toys")
public class SetVectorAction extends AbstractAction<SetVectorActionConfig> {
    private QuadrocopterThing quadrocopterThing;


    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    @Override
    public Object uniqueness() {
        return configuration.deviceUuid + configuration.title;
    }

    public SetVectorAction(BoxEngine engine, AbstractListOfTask<?> parent) {
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
        final Quadrocopter q = quadrocopterThing.getQuadrocopter();
        if (q == null) {
            return;
        }

        q.runTransition(
                configuration.transitionTimeMillis,
                configuration.useRoll ? configuration.rollValue : null,
                configuration.usePitch ? configuration.pitchValue : null,
                configuration.useYaw ? configuration.yawValue : null,
                configuration.useThrottle ? configuration.throttleValue : null
        );
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
