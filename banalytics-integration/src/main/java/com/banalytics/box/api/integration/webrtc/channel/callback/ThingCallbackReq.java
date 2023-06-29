package com.banalytics.box.api.integration.webrtc.channel.callback;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingCallbackReq extends AbstractMessage {
    Map<String, Object> params;

    public ThingCallbackReq() {
        super(MessageType.THNG_CLBK_REQ);
    }
}
