package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString(callSuper = true)
public class FindActionTasksRes extends AbstractChannelMessage {
    private Collection<NodeDescriptor> tasks;

    public FindActionTasksRes() {
        super(MessageType.FIND_ACT_TSKS_RES);
    }
}
