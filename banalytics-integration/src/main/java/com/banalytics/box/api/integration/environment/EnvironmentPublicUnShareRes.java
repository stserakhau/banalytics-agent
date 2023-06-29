package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentPublicUnShareRes extends AbstractEnvironmentShareMessage {
    private String userProfileEmail;

    private UUID environmentUUID;

    public EnvironmentPublicUnShareRes() {
        super(MessageType.ENV_PUB_UN_SHARE_RES);
    }

    public EnvironmentPublicUnShareRes(String userProfileEmail, UUID environmentUUID) {
        this();
        this.userProfileEmail = userProfileEmail;
        this.environmentUUID = environmentUUID;
    }
}
