package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentUnShareWithReq extends AbstractEnvironmentShareMessage {
    private String userProfileEmail;

    private UUID environmentUUID;

    public EnvironmentUnShareWithReq() {
        super(MessageType.ENV_UN_SHARE_WITH_REQ);
    }

    public EnvironmentUnShareWithReq(String userProfileEmail, UUID environmentUUID) {
        this();
        this.userProfileEmail = userProfileEmail;
        this.environmentUUID = environmentUUID;
    }
}
