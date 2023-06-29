package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class AbstractEnvironmentShareMessage extends AbstractMessage {
    public AbstractEnvironmentShareMessage(MessageType type) {
        super(type);
    }
}
