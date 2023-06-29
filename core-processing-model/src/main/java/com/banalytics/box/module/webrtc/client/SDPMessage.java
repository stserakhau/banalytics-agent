package com.banalytics.box.module.webrtc.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SDPMessage {
    String fromClientUuid;
    String type;
    String sdp;

    public SDPMessage() {
    }

    public SDPMessage(String fromClientUuid, String type, String sdp) {
        this.fromClientUuid = fromClientUuid;
        this.type = type;
        this.sdp = sdp;
    }
}
