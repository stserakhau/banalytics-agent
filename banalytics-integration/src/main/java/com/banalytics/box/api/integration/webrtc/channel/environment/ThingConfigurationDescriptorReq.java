package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingConfigurationDescriptorReq extends AbstractNodeRequest {
    private String className;

    public ThingConfigurationDescriptorReq() {
        super(MessageType.THNG_CNF_DSCR_REQ);
    }
}
