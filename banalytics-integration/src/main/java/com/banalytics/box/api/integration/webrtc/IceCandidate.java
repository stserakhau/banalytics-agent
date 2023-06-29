package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.MessageType;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
public class IceCandidate extends AbstractWebRTCMessage {
    public String candidate;
    public String sdpMid;
    public int sdpMLineIndex;

    public IceCandidate() {
        super(MessageType.candidate);
    }

    public IceCandidate(String candidate, String sdpMid, int sdpMLineIndex) {
        this();
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
