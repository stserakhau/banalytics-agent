package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.UUID;

public class ActionEvent extends AbstractEvent {
    @UIComponent(
            index = 0, type = ComponentType.drop_down, required = true,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc")
            }
    )
    public ActionState state;

    public String actionResult;

    @Override
    public String textView() {
        return StringSubstitutor.replace(
                """
                        *Action:* ${act}
                        *State:* ${state}
                        *Result:* ${result}
                        """,
                Map.of(
                        "act", this.getNodeTitle(),
                        "state", this.state,
                        "result", this.actionResult
                )
        );
    }

    public ActionEvent() {
        super(MessageType.EVT_ACTION);
    }

    public ActionEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, ActionState state, String actionResult) {
        super(MessageType.EVT_ACTION, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.state = state;
        this.actionResult = actionResult;
    }

    public enum ActionState {
        STARTING, COMPLETED
    }
}
