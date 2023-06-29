package com.banalytics.box.api.integration.websocket;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class YourSessionId extends AbstractMessage {
    public UUID sessionId;

    public YourSessionId() {
        super(MessageType.WS_YOUR_SESSION_ID);
    }

    public YourSessionId(UUID sessionId) {
        super(MessageType.WS_YOUR_SESSION_ID);
        this.sessionId = sessionId;
    }
}
