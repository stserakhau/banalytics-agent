package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.EmptyRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.FireActionReq;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.module.IAction;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class FireActionReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof FireActionReq req) {
            UUID taskUuid = req.getNodeUuid();
            IAction action = engine.findTask(taskUuid);
            if (action == null) {
                throw new Exception("action.error.removed");
            }
            ExecutionContext ctx = new ExecutionContext();
            ctx.setVar(IAction.MANUAL_RUN, IAction.MANUAL_RUN);
            action.action(ctx);

            EmptyRes res = new EmptyRes();
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
