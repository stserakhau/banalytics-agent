package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DeleteThingReq extends AbstractNodeRequest {

    public DeleteThingReq() {
        super(MessageType.DEL_THNG_REQ);
    }
}
