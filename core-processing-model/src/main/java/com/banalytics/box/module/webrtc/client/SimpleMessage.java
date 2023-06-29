package com.banalytics.box.module.webrtc.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleMessage {
    String fromClientUuid;
    String type;

    public SimpleMessage() {
    }

    public SimpleMessage(String fromClientUuid, String type) {
        this.fromClientUuid = fromClientUuid;
        this.type = type;
    }
}
