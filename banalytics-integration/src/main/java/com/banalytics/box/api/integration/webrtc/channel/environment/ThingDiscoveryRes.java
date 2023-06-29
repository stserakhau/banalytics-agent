package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;

public class ThingDiscoveryRes extends AbstractChannelMessage {
    public ThingDiscoveryRes() {
        super(MessageType.THNG_DSCVR_RES);
    }
}
