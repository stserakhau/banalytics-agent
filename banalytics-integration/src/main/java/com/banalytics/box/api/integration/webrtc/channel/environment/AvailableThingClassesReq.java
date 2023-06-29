package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AvailableThingClassesReq  extends AbstractChannelMessage {

    public AvailableThingClassesReq() {
        super(MessageType.AVL_THNG_CLSS_REQ);
    }
}