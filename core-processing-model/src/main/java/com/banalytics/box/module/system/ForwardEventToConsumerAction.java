package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.StatusEvent;
import com.banalytics.box.module.*;
import com.banalytics.box.module.events.EventHistoryThingConfig;
import com.banalytics.box.module.standard.EventConsumer;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.UUID;

import static com.banalytics.box.module.ConverterTypes.TYPE_SET_STRING;
import static com.banalytics.box.module.utils.Utils.nodeType;

public class ForwardEventToConsumerAction extends AbstractAction<ForwardEventToConsumerActionConfiguration> {
    public ForwardEventToConsumerAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private EventConsumer eventConsumer;

    private EventConsumer.Recipient recipient;

    @Override
    protected boolean isFireActionEvent() {
        return !(
                EventHistoryThingConfig.THING_UUID.equals(eventConsumer.getUuid())
                        || PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID.equals(eventConsumer.getUuid())
        );
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (eventConsumer == null) {
            onException(new Exception("error.eventConsumer.removed"));
        }
        AbstractEvent evt = executionContext.getVar(AbstractEvent.class);
        eventConsumer.consume(recipient, evt);
        return false;
    }

    @Override
    public void doAction(ExecutionContext ctx) throws Exception {
        if (ctx.getVar(IAction.MANUAL_RUN) != null) {
            ctx.setVar(AbstractEvent.class, new StatusEvent(
                    nodeType(this.getClass()),
                    this.getUuid(),
                    getSelfClassName(),
                    getTitle(),
                    NodeState.valueOf(getState().name()),
                    "Manual"
            ));
        }
        this.process(ctx);
    }

    @Override
    public String getTitle() {
        if (recipient == null || recipient.isEmpty()) {
            return "*";
        } else {
            StringBuilder sb = new StringBuilder(100);
            if (StringUtils.isNotEmpty(configuration.forwardToAccounts)) {
                if (eventConsumer == null) {
                    sb.append("error.eventConsumer.removed");
                } else {
                    try {
                        Set<String> accountIds = CommonUtils.DEFAULT_OBJECT_MAPPER.readValue(configuration.forwardToAccounts, TYPE_SET_STRING);
                        Set<String> names = eventConsumer.accountNames(accountIds);
                        sb.append(String.join(",", names));
                    } catch (JsonProcessingException e) {
                        sb.append(e.getMessage());
                        sendTaskState(e.getMessage());
                    }
                }
            }
            return sb.toString();
        }
    }

    @Override
    public UUID getSourceThingUuid() {
        if (eventConsumer == null) {
            return null;
        }
        return eventConsumer.getUuid();
    }

    @Override
    public void doInit() throws Exception {
        eventConsumer = engine.getThingAndSubscribe(configuration.eventConsumerThing, this);
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        Set<String> accounts = null;
        if (StringUtils.isNotEmpty(configuration.forwardToAccounts)) {
            accounts = CommonUtils.DEFAULT_OBJECT_MAPPER.readValue(configuration.forwardToAccounts, TYPE_SET_STRING);
        }
        recipient = new EventConsumer.Recipient(accounts);
    }

    @Override
    public void doStop() throws Exception {
    }

    @Override
    public void destroy() {
        if (eventConsumer != null) {
            ((Thing<?>) eventConsumer).unSubscribe(this);
            eventConsumer = null;
        }
    }
}
