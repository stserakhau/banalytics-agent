package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingsGroupsReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingsGroupsRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.IAction;
import com.banalytics.box.module.Singleton;

import java.util.ArrayList;
import java.util.List;

public class ThingsGroupsReqHandler implements ChannelRequestHandler {

    final BoxEngine engine;

    public ThingsGroupsReqHandler(BoxEngine engine) {
        this.engine = engine;
    }

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage req) throws Exception {
        if (req instanceof ThingsGroupsReq treq) {
            ThingsGroupsRes res = new ThingsGroupsRes();
            res.setRequestId(req.getRequestId());

            List<ThingsGroupsRes.ThingsGroup> groups = new ArrayList<>();

            for (Class<?> t : engine.supportedThings()) {
                if(Singleton.class.isAssignableFrom(t)){
                    continue;
                }
                groups.add(new ThingsGroupsRes.ThingsGroup(
                        t.getName()
                ));
            }
            groups.add(new ThingsGroupsRes.ThingsGroup(
                    Singleton.class.getName()
            ));
            groups.add(new ThingsGroupsRes.ThingsGroup(
                    IAction.class.getName()
            ));

            res.setGroups(groups);
            return res;
        }
        return null;
    }
}
