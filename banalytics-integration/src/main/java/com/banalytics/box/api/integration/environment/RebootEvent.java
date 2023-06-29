package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Event restart BanalyticsBox service
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RebootEvent extends AbstractDeviceRegistrationMessage {
    public RebootEvent() {
        super(MessageType.EVT_REBOOT);
    }
}
