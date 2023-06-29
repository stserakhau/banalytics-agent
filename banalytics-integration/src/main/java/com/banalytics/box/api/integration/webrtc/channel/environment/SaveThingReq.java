package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
public class SaveThingReq extends AbstractNodeRequest {
    private String thingClass;
    private Map<String, Object> configuration;

    public SaveThingReq() {
        super(MessageType.SAVE_THNG_REQ);
    }
}
