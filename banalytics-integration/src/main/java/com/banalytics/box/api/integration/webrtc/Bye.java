package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Bye extends AbstractWebRTCMessage {
    public Bye() {
        super(MessageType.bye);
    }
}
