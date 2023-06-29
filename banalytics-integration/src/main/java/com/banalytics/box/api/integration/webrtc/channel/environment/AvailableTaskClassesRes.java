package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString(callSuper = true)
@Deprecated
public class AvailableTaskClassesRes extends AbstractChannelMessage {
    Collection<String> taskClasses;

    public AvailableTaskClassesRes() {
        super(MessageType.AVL_TSK_CLSS_RES);
    }
}
