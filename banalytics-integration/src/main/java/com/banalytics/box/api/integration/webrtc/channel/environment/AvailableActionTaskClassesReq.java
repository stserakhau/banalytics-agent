package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AvailableActionTaskClassesReq extends AbstractChannelMessage {

    public AvailableActionTaskClassesReq() {
        super(MessageType.AVL_ACT_TSK_CLSS_REQ);
    }
}