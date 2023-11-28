package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.EnvironmentDescriptorReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.EnvironmentDescriptorRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Instance;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnvironmentDescriptorReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof EnvironmentDescriptorReq req) {
            EnvironmentDescriptorRes res = new EnvironmentDescriptorRes();
            res.setRequestId(req.getRequestId());

            Instance primaryInstance = engine.getPrimaryInstance();
            res.setPrimaryInstanceUuid(primaryInstance.getUuid());

            return res;
        }
        return null;
    }
}
