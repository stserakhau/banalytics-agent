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
public class AvailableSingletonThingClassesRes extends AbstractChannelMessage {
    Collection<String> availableSingletonClasses;

    public AvailableSingletonThingClassesRes() {
        super(MessageType.AVL_SNGL_THNG_CLSS_RES);
    }
}