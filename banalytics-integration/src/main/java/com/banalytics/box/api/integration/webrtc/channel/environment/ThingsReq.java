package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingsReq extends AbstractChannelMessage {
    Set<String> thingTypes;

    public ThingsReq() {
        super(MessageType.THNGS_REQ);
    }
}
