package com.banalytics.box.api.integration.webrtc.channel.environment.auth;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AuthenticationPasswordReq extends AbstractChannelMessage {
    private String password;

    public AuthenticationPasswordReq() {
        super(MessageType.AUTH_PWD_REQ);
    }

    @Override
    public boolean isAsyncAllowed() {
        return false;
    }
}
