package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentPublicShareReq extends AbstractEnvironmentShareMessage {
    private UUID environmentUUID;
    private String host;
    private String token;

    public EnvironmentPublicShareReq() {
        super(MessageType.ENV_PUB_SHARE_REQ);
    }

    public EnvironmentPublicShareReq(UUID environmentUUID, String host, String token) {
        this();
        this.environmentUUID = environmentUUID;
        this.host = host;
        this.token = token;
    }
}
