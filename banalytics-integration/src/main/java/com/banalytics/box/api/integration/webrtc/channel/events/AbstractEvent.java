package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractEvent extends AbstractMessage {
    private UUID messageUuid = UUID.randomUUID();
    private UUID environmentUuid;
    private NodeDescriptor.NodeType nodeType;
    private UUID nodeUuid;
    private String nodeClassName;
    private String nodeTitle;

    public AbstractEvent(MessageType type) {
        super(type);
    }

    public AbstractEvent(MessageType type, NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle) {
        super(type);
        this.nodeType = nodeType;
        this.nodeUuid = nodeUuid;
        this.nodeClassName = nodeClassName;
        this.nodeTitle = nodeTitle;
    }
}
