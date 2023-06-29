package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DeleteThingRes extends AbstractChannelMessage {
    private String result;

    public DeleteThingRes() {
        super(MessageType.DEL_THNG_RES);
    }
}
