package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.LocalizedException;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ExceptionMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.DeleteThingReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.DeleteThingRes;
import com.banalytics.box.module.BoxEngine;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteThingReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof DeleteThingReq req) {
            try {
                DeleteThingRes res = new DeleteThingRes();
                res.setRequestId(req.getRequestId());
                UUID thingUuid = req.getNodeUuid();
                engine.deleteThing(thingUuid);
                return res;
            } catch (Exception e) {
                ExceptionMessage error = new ExceptionMessage();
                error.setRequestId(request.getRequestId());
                if (e instanceof LocalizedException le) {
                    error.setMessage(le.getI18n());
                    error.setArgs(le.getArgs());
                } else {
                    error.setMessage(e.getMessage());
                }
                return error;
            }
        }
        return null;
    }
}
