package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableThingClassesReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableThingClassesRes;
import com.banalytics.box.module.BoxEngine;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AvailableThingClassesReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof AvailableThingClassesReq req) {
            Collection<Class<?>> supportedThings = engine.supportedThings();
            AvailableThingClassesRes res = new AvailableThingClassesRes();
            res.setThingClasses(supportedThings.stream().map(Class::getName).collect(Collectors.toSet()));
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}