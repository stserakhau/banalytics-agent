package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;

import java.util.UUID;

public class ActionEvent extends AbstractEvent {
    @UIComponent(
            index = 0, type = ComponentType.drop_down, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            }
    )
    public ActionState state;

    public ActionEvent() {
        super(MessageType.EVT_ACTION);
    }

    public ActionEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, ActionState state) {
        super(MessageType.EVT_ACTION, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.state = state;
    }

    public enum ActionState {
        STARTING, COMPLETED
    }
}
