package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.*;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionEvent extends AbstractEvent {
    public UUID sourceThingUuid;

    @UIComponent(
            index = 0, type = ComponentType.multi_select,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(
                            bean = "taskService",
                            method = "listPossibleConfigValues",
                            params = {
                                    "triggerAreas"
                            }
                    )
            }
    )
    public String[] zones;

    @UIComponent(
            index = 0, type = ComponentType.multi_select,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "show-empty", value = "false")
            },
            backendConfig = {
                    @UIComponent.BackendConfig(
                            bean = "taskService",
                            method = "listPossibleConfigValues",
                            params = {
                                    "classes"
                            }
                    )
            }
    )
    public String[] classes;

    @Override
    public String textView() {
        return StringSubstitutor.replace(
                """
                        Motion detected: ${source}
                        *Areas:* ${area}
                        *Classes:* ${classes}
                        """,
                Map.of(
                        "source", this.getNodeTitle(),
                        "area", Arrays.toString(this.zones),
                        "classes", Arrays.toString(classes)
                )
        );
    }

    public MotionEvent() {
        super(MessageType.EVT_MOTION);
    }

    public MotionEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, UUID sourceThingUuid, String[] zones, String[] classes) {
        super(MessageType.EVT_MOTION, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.sourceThingUuid = sourceThingUuid;
        this.zones = zones;
        this.classes = classes;
    }
}
