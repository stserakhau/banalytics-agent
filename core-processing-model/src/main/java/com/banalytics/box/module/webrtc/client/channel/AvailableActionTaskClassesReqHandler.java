package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableActionTaskClassesReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableActionTaskClassesRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableSingletonThingClassesReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableSingletonThingClassesRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.IAction;
import com.banalytics.box.module.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AvailableActionTaskClassesReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof AvailableActionTaskClassesReq req) {
            Collection<String> supportedThings = engine
                    .findTaskClassesByInterface(IAction.class)
                    .stream()
                    .map(Class::getName)
                    .collect(Collectors.toSet());

            AvailableActionTaskClassesRes res = new AvailableActionTaskClassesRes();
            res.setAvailableActionClasses(supportedThings);
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}