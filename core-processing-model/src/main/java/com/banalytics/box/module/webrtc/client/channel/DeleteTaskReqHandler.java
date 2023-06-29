package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.DeleteTaskReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.DeleteTaskRes;
import com.banalytics.box.module.BoxEngine;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteTaskReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof DeleteTaskReq req) {
            DeleteTaskRes res = new DeleteTaskRes();
            res.setRequestId(req.getRequestId());

            try {
                UUID taskUuid = req.getNodeUuid();
                engine.deleteTask(taskUuid);
            } catch (Exception e) {
                res.setResult(e.getMessage());
            }
            return res;
        }
        return null;
    }
}
