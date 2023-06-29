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
public class StartTaskReq extends AbstractNodeRequest {
    public StartTaskReq() {
        super(MessageType.STRT_TSK_REQ);
    }
}
