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
public class SystemMonitorEvent extends AbstractEvent {
    private Map<String, Object> general = new HashMap<>();
    private Map<String, Object> cpu = new HashMap<>();
    private Map<String, Object> ram = new HashMap<>();
    private Map<String, Object> disk = new HashMap<>();
    private Map<String, Object> net = new HashMap<>();

    public SystemMonitorEvent() {
        super(MessageType.EVT_SYS_MON);
    }

    public SystemMonitorEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle) {
        super(MessageType.EVT_SYS_MON, nodeType, nodeUuid, nodeClassName, nodeTitle);
    }
}
