package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentPublicUnShareReq extends AbstractEnvironmentShareMessage {
    private UUID environmentUUID;
    private String token;

    public EnvironmentPublicUnShareReq() {
        super(MessageType.ENV_PUB_UN_SHARE_REQ);
    }

    public EnvironmentPublicUnShareReq(UUID environmentUUID, String token) {
        this();
        this.environmentUUID = environmentUUID;
        this.token = token;
    }
}
