package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableSingletonThingClassesReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableSingletonThingClassesRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AvailableSingletonThingClassesReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof AvailableSingletonThingClassesReq req) {
            Collection<String> supportedThings = engine
                    .supportedThings()
                    .stream()
                    .filter(Singleton.class::isAssignableFrom)
                    .map(Class::getName)
                    .collect(Collectors.toSet());
            AvailableSingletonThingClassesRes res = new AvailableSingletonThingClassesRes();
            res.setAvailableSingletonClasses(supportedThings);
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}