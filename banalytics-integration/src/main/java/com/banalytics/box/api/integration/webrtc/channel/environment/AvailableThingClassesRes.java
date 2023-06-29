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
public class AvailableThingClassesRes extends AbstractChannelMessage {
    Collection<String> thingClasses;

    public AvailableThingClassesRes() {
        super(MessageType.AVL_THNG_CLSS_RES);
    }
}