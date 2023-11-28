package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingConfigurationDescriptorReq;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingConfigurationDescriptorRes;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Thing;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ThingConfigurationDescriptorReqHandler implements ChannelRequestHandler {
    private final BoxEngine engine;

    @Override
    public ChannelMessage handle(ChannelMessage request) throws Exception {
        if (request instanceof ThingConfigurationDescriptorReq req) {
            UUID thingUuid = req.getNodeUuid();
            String className = req.getClassName();

            ThingConfigurationDescriptorRes res = new ThingConfigurationDescriptorRes();

            if (thingUuid != null) {
                Thing<?> thing = engine.getThing(thingUuid);
                if (thing == null) {
                    throw new Exception("thing.error.notFound");
                }
                res.setConfiguration(thing.getConfiguration());
                res.setHasSubscribers(!thing.getSubscribers().isEmpty());
            }

            FormModel formModel = engine.describeClass(className);
            res.setFormModel(formModel);

            res.setRequestId(req.getRequestId());
            return res;
        }
        return null;
    }
}
