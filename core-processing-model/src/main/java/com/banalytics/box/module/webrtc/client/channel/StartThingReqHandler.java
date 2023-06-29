package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.EmptyRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.StartThingReq;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class StartThingReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof StartThingReq req) {
            UUID thingUuid = req.getNodeUuid();

            if(!UserThreadContext.hasStartStopPermission(thingUuid)) {
                throw new Exception("startStopDenied");
            }

            engine.startThing(thingUuid);

            EmptyRes res = new EmptyRes();
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
