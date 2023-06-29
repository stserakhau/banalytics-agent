package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.TaskConfigurationDescriptorReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingConfigurationDescriptorRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class TaskConfigurationDescriptorReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public AbstractChannelMessage handle(AbstractChannelMessage request) throws Exception {
        if (request instanceof TaskConfigurationDescriptorReq req) {
            UUID taskUuid = req.getNodeUuid();
            final String className;

            ThingConfigurationDescriptorRes res = new ThingConfigurationDescriptorRes();


            if (taskUuid != null) {
                ITask<?> task = engine.findTask(taskUuid);
                if (task == null) {
                    throw new Exception("task.error.removed");
                }
                res.setConfiguration(task.getConfiguration());
                res.setParentTaskUuid(task.parent().getUuid());
                className = task.getSelfClassName();
            } else {
                res.setParentTaskUuid(engine.getPrimaryInstance().getUuid());
                className = req.getClassName();
            }

            FormModel formModel = engine.describeClass(className);
            res.setFormModel(formModel);
            res.setClassName(className);
            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
