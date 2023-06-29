package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.SaveThingReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.SaveThingRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.constants.RestartOnFailure;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static com.banalytics.box.module.utils.Utils.nodeType;

@RequiredArgsConstructor
public class SaveThingReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof SaveThingReq req) {
            UUID thingUuid = req.getNodeUuid();
            String thingClass = req.getThingClass();
            Map<String, Object> configuration = req.getConfiguration();

            boolean wasCreated = thingUuid == null;

            Thing<?> thing = engine.saveOrUpdateThing(thingUuid, thingClass, configuration);

            SaveThingRes res = new SaveThingRes();
            res.setCreated(wasCreated);
            res.setRequestId(req.getRequestId());
            res.setThing(new NodeDescriptor(
                    thing.getUuid(),
                    thing.getSelfClassName(),
                    thing.getTitle(),
                    thing.getUuid(),
                    nodeType(thing.getClass()),
                    true,
                    !thing.getSubscribers().isEmpty(),
                    false,
                    NodeState.valueOf(thing.getState().name()),
                    thing.getStateDescription(),
                    thing.getConfiguration().getRestartOnFailure() != RestartOnFailure.STOP_ON_FAILURE,
                    thing instanceof Singleton
            ));
            return res;
        }
        return null;
    }
}
