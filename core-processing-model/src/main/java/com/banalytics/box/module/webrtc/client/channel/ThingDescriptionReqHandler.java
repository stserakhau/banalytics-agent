package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingDescriptionReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingDescriptionRes;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.RestartOnFailure;
import com.banalytics.box.module.webrtc.client.UserThreadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.banalytics.box.module.utils.Utils.*;

public class ThingDescriptionReqHandler implements ChannelRequestHandler {

    final BoxEngine engine;

    public ThingDescriptionReqHandler(BoxEngine engine) {
        this.engine = engine;
    }

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage req) throws Exception {
        if (req instanceof ThingDescriptionReq treq) {
            UUID thingUuid = treq.getNodeUuid();
            Thing<?> thing = engine.getThing(thingUuid);
            if (thing == null) {
                throw new Exception("thing.error.removed");
            }
            List<NodeDescriptor> relatedTasks = new ArrayList<>();
            thing.getSubscribers().forEach(subscriber -> {
                if (subscriber instanceof ITask<?> task) {

                    if(subscriber instanceof IAction){
                        if(!UserThreadContext.isMyEnvironment() && !UserThreadContext.hasActionPermission(thingUuid)) {
                            return;
                        }
                    }

                    relatedTasks.add(new NodeDescriptor(
                            task.getUuid(),
                            task.getSelfClassName(),
                            task.getTitle(),
                            task.getSourceThingUuid(),
                            nodeType(task.getClass()),
                            isSupportsSubtasks(task.getClass()),
                            (task instanceof AbstractListOfTask mt && !mt.getSubTasks().isEmpty()),
                            isSupportsMediaStream(task.getClass()),
                            NodeState.valueOf(task.getState().name()),
                            task.getStateDescription(),
                            task.getConfiguration().getRestartOnFailure() != RestartOnFailure.STOP_ON_FAILURE,
                            task instanceof Singleton
                    ));
                }
            });
            ThingDescriptionRes res = new ThingDescriptionRes();
            res.setRequestId(req.getRequestId());
            res.setTitle(thing.getTitle());
            res.setClassName(thing.getSelfClassName());
            res.setTasks(relatedTasks);
            res.setOptions(thing.options());
            res.setGeneralPermissions(thing.generalPermissions());
            res.setApiMethodsPermissions(thing.apiMethodsPermissions());
            return res;
        }

        return null;
    }

//    private void accumulateTasks(ITask<?> task, List<NodeDescriptor> taskAccumulator) {
//        taskAccumulator.add(new NodeDescriptor(
//                task.getUuid(),
//                task.getSelfClassName(),
//                task.getTitle(),
//                task instanceof IAction,
//                isSupportsSubtasks(task.getClass()),
//                isSupportsMediaStream(task.getClass()),
//                task.isEnabled(),
//                NodeState.valueOf(task.getState().name()),
//                task.getStateDescription(),
//                task.getConfiguration().getRestartOnFailure() != RestartOnFailure.STOP_ON_FAILURE
//        ));
//        if (task instanceof AbstractListOfTask<?> nodeTask) {
//            for (AbstractTask<?> subtask : nodeTask.getSubTasks()) {
//                accumulateTasks(subtask, taskAccumulator);
//            }
//        }
//    }
}
