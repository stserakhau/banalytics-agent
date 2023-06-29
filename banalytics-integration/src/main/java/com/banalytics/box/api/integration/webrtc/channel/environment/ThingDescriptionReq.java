package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingDescriptionReq extends AbstractNodeRequest {
    public ThingDescriptionReq() {
        super(MessageType.THNG_DSCR_REQ);
    }
}
