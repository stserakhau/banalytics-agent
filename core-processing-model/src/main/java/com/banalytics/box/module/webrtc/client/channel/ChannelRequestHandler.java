package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;

public interface ChannelRequestHandler {
    ChannelMessage handle(ChannelMessage request) throws Exception;
}
