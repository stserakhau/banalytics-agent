package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AvailableSingletonThingClassesReq extends AbstractChannelMessage {

    public AvailableSingletonThingClassesReq() {
        super(MessageType.AVL_SNGL_THNG_CLSS_REQ);
    }
}