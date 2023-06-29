package com.banalytics.box.module;

import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;

import static com.banalytics.box.module.utils.Utils.nodeType;

public abstract class AbstractAction<CONFIGURATION extends IConfiguration> extends AbstractTask<CONFIGURATION> implements IAction {
    public AbstractAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public abstract void doAction(ExecutionContext ctx) throws Exception;

    protected abstract boolean isFireActionEvent();

    @Override
    public void action(ExecutionContext ctx) throws Exception {
        if(isFireActionEvent()) {
            engine.fireEvent(new ActionEvent(
                    nodeType(this.getClass()),
                    this.getUuid(),
                    getSelfClassName(),
                    getTitle(),
                    ActionEvent.ActionState.STARTING
            ));
        }
        doAction(ctx);
        if(isFireActionEvent()) {
            engine.fireEvent(new ActionEvent(
                    nodeType(this.getClass()),
                    this.getUuid(),
                    getSelfClassName(),
                    getTitle(),
                    ActionEvent.ActionState.COMPLETED
            ));
        }
    }
}
