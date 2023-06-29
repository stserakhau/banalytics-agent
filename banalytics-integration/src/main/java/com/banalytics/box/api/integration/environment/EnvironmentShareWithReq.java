package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentShareWithReq extends AbstractEnvironmentShareMessage {
    private String userProfileEmail;
    private String password;
    private UUID environmentUUID;

    public EnvironmentShareWithReq() {
        super(MessageType.ENV_SHARE_WITH_REQ);
    }

    public EnvironmentShareWithReq(String userProfileEmail, String password, UUID environmentUUID) {
        this();
        this.userProfileEmail = userProfileEmail;
        this.password = password;
        this.environmentUUID = environmentUUID;
    }
}
