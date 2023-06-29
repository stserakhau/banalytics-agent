package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;

public class ThingDiscoveryReq extends AbstractChannelMessage {
    public ThingDiscoveryReq() {
        super(MessageType.THNG_DSCVR_REQ);
    }
}
