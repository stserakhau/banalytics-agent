package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractWebRTCMessage extends AbstractMessage {
    public String clientWebSocketSession;
    public UUID fromAgentUuid;
    public UUID toAgentUuid;
    public boolean fromMyAccount;
    public Long fromAccountId;
    public String fromAccountEmail;

    public boolean webConnection;

    public AbstractWebRTCMessage(MessageType type) {
        super(type);
    }
}
