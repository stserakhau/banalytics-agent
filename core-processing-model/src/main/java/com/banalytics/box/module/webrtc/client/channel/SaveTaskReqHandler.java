package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import com.banalytics.box.api.integration.webrtc.channel.environment.SaveTaskReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.SaveTaskRes;
import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.RestartOnFailure;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static com.banalytics.box.module.utils.Utils.*;

@RequiredArgsConstructor
public class SaveTaskReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof SaveTaskReq req) {
            UUID parentTaskUuid = req.getParentTaskUuid();
            UUID taskUuid = req.getNodeUuid();
            String taskClass = req.getTaskClass();
            Map<String, Object> configuration = req.getConfiguration();

            boolean wasCreated = taskUuid == null;

            AbstractTask<?> i = engine.saveOrUpdateTask(parentTaskUuid, taskUuid, taskClass, configuration);

            SaveTaskRes res = new SaveTaskRes();
            res.setCreated(wasCreated);
            res.setTask(new NodeDescriptor(i.getUuid(),
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
                    i instanceof Singleton));
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
