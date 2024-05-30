package com.banalytics.box.module;

import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;

import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.module.utils.Utils.nodeType;

public abstract class AbstractAction<CONFIGURATION extends IConfiguration> extends AbstractTask<CONFIGURATION> implements IAction {
    public AbstractAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public abstract String doAction(ExecutionContext ctx) throws Exception;

    protected abstract boolean isFireActionEvent();

    @Override
    protected boolean canStart() {
        return true;
    }

    @Override
    public void action(ExecutionContext ctx) throws Exception {
        if (this.state != State.RUN) {
            return;
        }
        if (isFireActionEvent()) {
            engine.fireEvent(new ActionEvent(
                    nodeType(this.getClass()),
                    this.getUuid(),
                    getSelfClassName(),
                    getTitle(),
                    ActionEvent.ActionState.STARTING,
                    null
            ));
        }
        String operationResult = doAction(ctx);
        if (isFireActionEvent()) {
            engine.fireEvent(new ActionEvent(
                    nodeType(this.getClass()),
                    this.getUuid(),
                    getSelfClassName(),
                    getTitle(),
                    ActionEvent.ActionState.COMPLETED,
                    operationResult
            ));
        }
    }
}
