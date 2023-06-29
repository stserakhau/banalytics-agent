package com.banalytics.box.api.integration.webrtc.channel.environment.auth;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AuthenticationTokenReq extends AbstractChannelMessage {
    private String token;

    public AuthenticationTokenReq() {
        super(MessageType.AUTH_TKN_REQ);
    }

    @Override
    public boolean isAsyncAllowed() {
        return false;
    }
}
