package com.banalytics.box.api.integration.webrtc.channel.callback;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingCallbackRes extends AbstractMessage {
    Object result;

    public ThingCallbackRes() {
        super(MessageType.THNG_CLBK_RES);
    }
}
