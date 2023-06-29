package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class RegisterMeRequest extends AbstractDeviceRegistrationMessage {
    public RegisterMeRequest() {
        super(MessageType.REGISTER_ME_REQ);
    }

    UUID productUUID;

    String environmentHash;

    /**
     * {@link  com.banalytics.box.api.integration.environment.OSType}
     */
    OSType os;

    EnvironmentType deviceType;
}
