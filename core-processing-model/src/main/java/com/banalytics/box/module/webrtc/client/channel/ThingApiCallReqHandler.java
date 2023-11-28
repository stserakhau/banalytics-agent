package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.webrtc.client.RTCClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class ThingApiCallReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;
    private final RTCClient rtcClient;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof ThingApiCallReq req) {
//            log.info("Request:\n{}", req);
            UUID thingUuid = req.getNodeUuid();
            Map<String, Object> params = req.getParams();

            Thing<?> thing = engine.getThing(thingUuid);
            if (thing == null) {
                throw new Exception("thing.error.removed");
            }
            Object response = thing.call(params);
            if (response instanceof Thing.DownloadStream downloadStream) {
                String fileResourceId = thingUuid + ":/" + params.get("contextPath");
                rtcClient.sendFile(thingUuid, fileResourceId, req.getSequenceIdentifier(), downloadStream);
                return null;
            } else {
                ThingApiCallRes res = new ThingApiCallRes();
                res.setResponse(response);
                res.setRequestId(req.getRequestId());
//                log.info("Response:\n{}", res);
                return res;
            }
        }
        return null;
    }

    public record FileDownloadResponse(boolean cancelled, String message) {
    }
}
