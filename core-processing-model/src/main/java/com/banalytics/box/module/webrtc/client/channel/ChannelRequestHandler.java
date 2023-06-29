package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;

public interface ChannelRequestHandler {
    AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception;
}
