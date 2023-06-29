package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class SubTasksRes extends AbstractChannelMessage {
    private List<NodeDescriptor> subTasks;

    public SubTasksRes() {
        super(MessageType.SBTSK_RES);
    }

    public SubTasksRes(List<NodeDescriptor> subTasks) {
        this();
        this.subTasks = subTasks;
    }
}
