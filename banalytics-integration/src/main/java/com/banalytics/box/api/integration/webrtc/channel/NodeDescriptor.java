package com.banalytics.box.api.integration.webrtc.channel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class NodeDescriptor {
    private UUID environmentUuid;
    private UUID uuid;
    private String className;
    private String title;
    private UUID sourceThingUuid;
    private NodeType nodeType;
    private boolean supportsSubTasks;
    private boolean hasChildren;
    private boolean hasMediaStream;
    private NodeState state;
    private String stateDescription;
    private boolean restartOnFailure;
    private boolean singleton;
    private boolean removable = true;

    public NodeDescriptor() {
    }

    public NodeDescriptor(UUID uuid) {
        this.uuid = uuid;
    }

    public NodeDescriptor(UUID uuid, String className, String title, UUID sourceThingUuid, NodeType nodeType,
                          boolean supportsSubTasks, boolean hasChildren, boolean hasMediaStream,
                          NodeState state, String stateDescription, boolean restartOnFailure,
                          boolean singleton) {
        this.uuid = uuid;
        this.className = className;
        this.title = title;
        this.sourceThingUuid = sourceThingUuid;
        this.nodeType = nodeType;
        this.supportsSubTasks = supportsSubTasks;
        this.hasChildren = hasChildren;
        this.hasMediaStream = hasMediaStream;
        this.state = state;
        this.stateDescription = stateDescription;
        this.restartOnFailure = restartOnFailure;
        this.singleton = singleton;
    }

    public enum NodeType {
        THING, TASK, ACTION
    }
}
