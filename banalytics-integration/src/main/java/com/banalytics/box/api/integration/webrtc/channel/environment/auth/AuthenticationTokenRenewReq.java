package com.banalytics.box.api.integration.webrtc.channel.environment.auth;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AuthenticationTokenRenewReq extends AbstractChannelMessage {
    String newToken;

    public AuthenticationTokenRenewReq() {
        super(MessageType.AUTH_TKN_RENEW_REQ);
    }
}
