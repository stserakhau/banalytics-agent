package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class ReadyToLinkResponse extends AbstractDeviceRegistrationMessage {

    UUID yourUuid;

    public ReadyToLinkResponse() {
        super(MessageType.READY_TO_LINK);
    }

    public ReadyToLinkResponse(UUID yourUuid) {
        this();
        this.yourUuid = yourUuid;
    }
}
