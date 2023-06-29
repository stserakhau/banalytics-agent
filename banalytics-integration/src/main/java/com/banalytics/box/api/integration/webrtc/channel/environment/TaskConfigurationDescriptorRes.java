package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TaskConfigurationDescriptorRes extends AbstractChannelMessage {
    Object configuration;
    FormModel formModel;

    public TaskConfigurationDescriptorRes() {
        super(MessageType.TSK_CNF_DSCR_RES);
    }
}
