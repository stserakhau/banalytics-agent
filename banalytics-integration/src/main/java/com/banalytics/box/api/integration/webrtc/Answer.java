package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Answer extends AbstractWebRTCMessage {

    public String sdp;

    public Answer() {
        super(MessageType.answer);
    }

    public Answer(String sdp) {
        this();
        this.sdp = sdp;
    }
}
