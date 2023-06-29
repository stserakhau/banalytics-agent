package com.banalytics.box.api.integration.suc;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class AbstractSUCIntegrationMessage extends AbstractMessage {
    public AbstractSUCIntegrationMessage(MessageType type) {
        super(type);
    }
}
