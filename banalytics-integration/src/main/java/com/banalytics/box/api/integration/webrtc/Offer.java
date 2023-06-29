package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Offer extends AbstractWebRTCMessage {
    public String sdp;

    public String[] iceServersList;

    public Offer() {
        super(MessageType.offer);
    }

    public Offer(String sdp) {
        this();
        this.sdp = sdp;
    }
}
