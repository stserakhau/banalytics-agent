package com.banalytics.box.api.integration.webrtc.channel;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractChannelMessage extends AbstractMessage {
    int requestId;

    public AbstractChannelMessage(MessageType type) {
        super(type);
    }

    public boolean isAsyncAllowed() {
        return true;
    }
}
