package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString(callSuper = true)
public class FindActionTasksReq extends AbstractChannelMessage {
    Set<String> actionClasses;

    public FindActionTasksReq() {
        super(MessageType.FIND_ACT_TSKS_REQ);
    }
}
