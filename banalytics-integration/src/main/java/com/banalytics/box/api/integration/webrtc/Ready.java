package com.banalytics.box.api.integration.webrtc;

import com.banalytics.box.api.integration.MessageType;

public class Ready extends AbstractWebRTCMessage {
    public Ready() {
        super(MessageType.ready);
    }
}
