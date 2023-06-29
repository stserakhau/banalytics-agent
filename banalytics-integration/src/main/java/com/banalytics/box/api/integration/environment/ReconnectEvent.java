package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.environment.AbstractDeviceRegistrationMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Event sends from Portal to Environment when user added / removed or set upgrade to modules
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ReconnectEvent extends AbstractDeviceRegistrationMessage {
    public ReconnectEvent() {
        super(MessageType.EVT_RECONNECT);
    }
}
