package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingTasksRes extends AbstractChannelMessage {
    List<ThingDescriptor> thingsDescriptors;

    public ThingTasksRes() {
        super(MessageType.THNG_TSKS_RES);
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class ThingDescriptor {
        UUID thingUuid;
        String thingTitle;
        String thingClassName;
        Map<String, ?> thingOptions;

        List<?> tasks;
    }
}
