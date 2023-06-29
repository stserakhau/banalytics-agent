package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingConfigurationDescriptorRes extends AbstractChannelMessage {
    UUID parentTaskUuid;
    String className;
    Object configuration;
    FormModel formModel;
    boolean hasSubscribers;

    public ThingConfigurationDescriptorRes() {
        super(MessageType.THNG_CNF_DSCR_RES);
    }
}
