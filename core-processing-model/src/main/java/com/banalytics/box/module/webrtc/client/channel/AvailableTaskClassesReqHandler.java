package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableTaskClassesReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.AvailableTaskClassesRes;
import com.banalytics.box.module.BoxEngine;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@Deprecated
public class AvailableTaskClassesReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        /*if (request instanceof AvailableTaskClassesReq req) {
            UUID parentTaskUuid = req.getParentTaskUuid();

            Collection<String> supportedSubtasks = engine.supportedSubtasks(parentTaskUuid);

            AvailableTaskClassesRes res = new AvailableTaskClassesRes();
            res.setTaskClasses(supportedSubtasks);
            res.setRequestId(req.getRequestId());
            return res;
        }*/
        return null;
    }
}