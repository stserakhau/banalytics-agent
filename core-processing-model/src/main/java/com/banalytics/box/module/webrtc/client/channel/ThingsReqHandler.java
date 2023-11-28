package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.model.SharePermission;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingsReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingsRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.Thing;

import java.util.*;

import static com.banalytics.box.module.utils.Utils.nodeType;
import static com.banalytics.box.module.webrtc.client.channel.Constants.ALWAYS_REQUIRED_THINGS_UUID_SET;

public class ThingsReqHandler implements ChannelRequestHandler {

    final BoxEngine engine;
    final Map<UUID, SharePermission> clientPermissions;

    public ThingsReqHandler(BoxEngine engine, Map<UUID, SharePermission> clientPermissions) {
        this.engine = engine;
        this.clientPermissions = clientPermissions;
    }

    @Override
    public ChannelMessage handle(ChannelMessage req) throws Exception {
        if (req instanceof ThingsReq treq) {
            ThingsRes res = new ThingsRes();
            res.setRequestId(req.getRequestId());

            List<NodeDescriptor> result = new ArrayList<>();

            Set<String> typesRequest = treq.getThingTypes();
            boolean hasSingletonRequest = typesRequest == null || typesRequest.contains(Singleton.class.getName());
            for (Thing<?> t : engine.findThings()) {
                if (typesRequest != null) {//if types specified filter things by type
                    if (
                            hasSingletonRequest && t instanceof Singleton
                                    || treq.getThingTypes().contains(t.getSelfClassName())
                    ) {
                        //do nothing
                    } else {
                        continue;
                    }
                }
                //if permissions is null it means that is my env, otherwise check permissions
                if (clientPermissions != null && !clientPermissions.containsKey(t.getUuid())) {
                    continue;
                }
                NodeDescriptor nd = new NodeDescriptor(
                        t.getUuid(),
                        t.getSelfClassName(),
                        t.getTitle(),
                        t.getUuid(),
                        nodeType(t.getClass()),
                        false,
                        !t.getSubscribers().isEmpty(),
                        false,
                        NodeState.valueOf(t.getState().name()),
                        t.getStateDescription(),
                        false,
                        t instanceof Singleton
                );
                nd.setRemovable(!ALWAYS_REQUIRED_THINGS_UUID_SET.contains(t.getUuid()));
                result.add(nd);
            }

            res.setThings(result);
            return res;
        }
        return null;
    }
}
