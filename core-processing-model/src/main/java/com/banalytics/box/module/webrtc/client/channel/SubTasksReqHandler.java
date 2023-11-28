package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.SubTasksReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.SubTasksRes;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.RestartOnFailure;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.banalytics.box.module.utils.Utils.*;

@RequiredArgsConstructor
public class SubTasksReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof SubTasksReq req) {
            UUID parentTaskUuid = req.getNodeUuid();
            final Collection<AbstractTask<?>> subtasks;
            if (parentTaskUuid == null) {
                subtasks = engine.instances();
            } else {
                subtasks = engine.findSubTasks(parentTaskUuid);
            }

            SubTasksRes res = new SubTasksRes(
                    subtasks.stream()
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
                            .collect(Collectors.toList())
            );
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
