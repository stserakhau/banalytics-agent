package com.banalytics.box.module.webrtc.client.channel.measurement;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.KeyboardEvent;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.webrtc.client.channel.ChannelRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KeyboardEventHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof KeyboardEvent event) {
            log.info("Keyboard event received:\n{}", event);
            engine.fireEvent(event);
        }
        return null;
    }
}
