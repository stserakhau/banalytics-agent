package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingDescriptionRes extends AbstractChannelMessage {
    String title;
    String className;
    List<NodeDescriptor> tasks;
    Map<String, ?> options;
    Set<String> generalPermissions;
    Set<String> apiMethodsPermissions;

    public ThingDescriptionRes() {
        super(MessageType.THNG_DSCR_RES);
    }
}
