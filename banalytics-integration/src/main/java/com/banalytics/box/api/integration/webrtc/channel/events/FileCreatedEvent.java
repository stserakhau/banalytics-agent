package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class FileCreatedEvent extends AbstractEvent {
    UUID storageUuid;
    String contextPath;
    Map<String, Object> options = new HashMap<>(4);

    public FileCreatedEvent() {
        super(MessageType.EVT_FILE_CRT);
    }

    public FileCreatedEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, UUID storageUuid, String contextPath) {
        super(MessageType.EVT_FILE_CRT, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.storageUuid = storageUuid;
        this.contextPath = contextPath;
    }

    public void option(String name, Object value) {
        options.put(name, value);
    }
    public <T> T option(String name) {
        return (T) options.get(name);
    }
}
