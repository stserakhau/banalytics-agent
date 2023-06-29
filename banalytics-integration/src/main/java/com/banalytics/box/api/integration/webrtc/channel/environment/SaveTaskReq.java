package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class SaveTaskReq extends AbstractNodeRequest {
    private UUID parentTaskUuid;
    private String taskClass;
    private Map<String, Object> configuration;

    public SaveTaskReq() {
        super(MessageType.SAVE_TSK_REQ);
    }
}
