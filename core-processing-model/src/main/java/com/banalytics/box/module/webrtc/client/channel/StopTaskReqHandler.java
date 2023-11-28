package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.EmptyRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.StopTaskReq;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class StopTaskReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof StopTaskReq req) {
            UUID taskUuid = req.getNodeUuid();

            ITask<?> task = engine.findTask(taskUuid);
            if(!UserThreadContext.hasStartStopPermission(task.getSourceThingUuid())) {
                throw new Exception("startStopDenied");
            }

            engine.stopTask(taskUuid);

            EmptyRes res = new EmptyRes();
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
