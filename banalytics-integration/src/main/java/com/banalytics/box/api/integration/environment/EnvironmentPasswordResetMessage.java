package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentPasswordResetMessage extends AbstractEnvironmentShareMessage {
    private String userProfileEmail;
    private String password;
    private UUID environmentUUID;

    public EnvironmentPasswordResetMessage() {
        super(MessageType.ENV_PWD_RST_MSG);
    }

    public EnvironmentPasswordResetMessage(String userProfileEmail, String password, UUID environmentUUID) {
        this();
        this.userProfileEmail = userProfileEmail;
        this.password = password;
        this.environmentUUID = environmentUUID;
    }
}
