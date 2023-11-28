package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingTasksReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingTasksRes;
import com.banalytics.box.model.task.EnvironmentNode;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import com.banalytics.box.module.InitShutdownSupport;
import com.banalytics.box.module.Thing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ThingTasksReqHandler implements ChannelRequestHandler {

    final BoxEngine engine;

    public ThingTasksReqHandler(BoxEngine engine) {
        this.engine = engine;
    }

    @Override
    public ChannelMessage handle(ChannelMessage req) throws Exception {
        if (req instanceof ThingTasksReq treq) {
            UUID thingUuid = treq.getNodeUuid();
            final Collection<? extends Thing<?>> things;
            if (thingUuid == null) {
                things = engine.findThings();
            } else {
                Thing<?> thing = engine.getThing(thingUuid);
                if (thing == null) {
                    throw new Exception("thing.error.removed");
                }
                things = List.of(thing);
            }

            ThingTasksRes res = new ThingTasksRes();
            res.setRequestId(req.getRequestId());

            List<ThingTasksRes.ThingDescriptor> thDescrs = new ArrayList<>();
            for (Thing<?> thing : things) {
                List<EnvironmentNode> relatedTasks = new ArrayList<>();
                ThingTasksRes.ThingDescriptor descr = new ThingTasksRes.ThingDescriptor();
                descr.setThingUuid(thing.getUuid());
                descr.setThingTitle(thing.getTitle());
                descr.setThingClassName(thing.getSelfClassName());
                descr.setThingOptions(thing.options());
                for (InitShutdownSupport subscriber : thing.getSubscribers()) {
                    if (subscriber instanceof ITask<?> task) {
                        relatedTasks.add(EnvironmentNode.build(task));
                    }
                }
                descr.setTasks(relatedTasks);
                thDescrs.add(descr);
            }
            res.setThingsDescriptors(thDescrs);
            return res;
        }

        return null;
    }
}
