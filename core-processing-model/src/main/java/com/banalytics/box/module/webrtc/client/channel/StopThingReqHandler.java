package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.EmptyRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.StopTaskReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.StopThingReq;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class StopThingReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof StopThingReq req) {
            UUID thingUuid = req.getNodeUuid();

            if(!UserThreadContext.hasStartStopPermission(thingUuid)) {
                throw new Exception("startStopDenied");
            }

            engine.stopThing(thingUuid);

            EmptyRes res = new EmptyRes();
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
