package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentDescriptorRes extends AbstractChannelMessage {
    UUID primaryInstanceUuid;

    public EnvironmentDescriptorRes() {
        super(MessageType.ENV_DESCR_RES);
    }
}
