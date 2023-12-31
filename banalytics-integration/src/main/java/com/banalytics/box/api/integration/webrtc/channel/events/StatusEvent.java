package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.NodeState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class StatusEvent extends AbstractEvent {
    @UIComponent(
            index = 0, type = ComponentType.multi_select, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            }
    )
    public NodeState state;

    String message;

    @Override
    public String textView() {
        return StringSubstitutor.replace(
                "*${type}*: *${state}: ${nodeTitle}* ${nodeClass} ```${message}```",
                Map.of(
                        "type", this.getType(),
                        "nodeType", this.getNodeType(),
                        "nodeTitle", this.getNodeTitle(),
                        "nodeClass", this.getNodeClassName(),
                        "state", this.getState(),
                        "message", this.getMessage()
                )
        );
    }

    public StatusEvent() {
        super(MessageType.EVT_STATUS);
    }

    public StatusEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, NodeState state, String message) {
        super(MessageType.EVT_STATUS, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.state = state;
        this.message = message;
    }
}
