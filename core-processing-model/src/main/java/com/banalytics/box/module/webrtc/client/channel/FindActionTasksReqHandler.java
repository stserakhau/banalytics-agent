package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.FindActionTasksReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.FindActionTasksRes;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.RestartOnFailure;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.banalytics.box.module.utils.Utils.*;

@RequiredArgsConstructor
public class FindActionTasksReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof FindActionTasksReq req) {
            FindActionTasksRes res = new FindActionTasksRes();
            res.setRequestId(req.getRequestId());

            Set<String> actionClasses = req.getActionClasses();

            Collection<AbstractTask<?>> actions = engine.findActionTasks();

            if (actionClasses != null && !actionClasses.isEmpty()) {//action group filter
                actions = actions.stream()
                        .filter(action -> actionClasses.contains(action.getSelfClassName()))
                        .collect(Collectors.toSet());
            }

            //security action filter
            if(!UserThreadContext.isMyEnvironment()) {
                actions = actions.stream()
                        .filter(action -> {
                            Thing<?> sourceThing = action.getSourceThing();
                            if (sourceThing == null) {
                                return false;
                            }
                            return UserThreadContext.hasActionPermission(sourceThing.getUuid());
                        })
                        .collect(Collectors.toSet());
            }

            res.setTasks(actions
                    .stream()
                    .map(i -> new NodeDescriptor(
                            i.getUuid(),
                            i.getSelfClassName(),
                            i.getTitle(),
                            i.getSourceThingUuid(),
                            nodeType(i.getClass()),
                            isSupportsSubtasks(i.getClass()),
                            (i instanceof AbstractListOfTask mt && !mt.getSubTasks().isEmpty()),
                            isSupportsMediaStream(i.getClass()),
                            NodeState.valueOf(i.getState().name()),
                            i.stateDescription,
                            i.configuration.getRestartOnFailure() != RestartOnFailure.STOP_ON_FAILURE,
                            i instanceof Singleton
                    ))
                    .collect(Collectors.toList()));

            return res;
        }
        return null;
    }
}
