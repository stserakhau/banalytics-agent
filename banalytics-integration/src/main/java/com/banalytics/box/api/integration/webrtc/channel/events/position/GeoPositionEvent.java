package com.banalytics.box.api.integration.webrtc.channel.events.position;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class GeoPositionEvent extends AbstractEvent {
    public double latitude;

    public double longitude;

    public int altitude;

    @UIComponent(
            index = 0, type = ComponentType.text_input
    )
    public String nativeRule = "";// todo predefined name for EventManager

    public GeoPositionEvent() {
        super(MessageType.EVT_GEO_POS);
    }

    public GeoPositionEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle) {
        super(MessageType.EVT_GEO_POS, nodeType, nodeUuid, nodeClassName, nodeTitle);
    }

    @Override
    public String textView() {
        return super.textView() + StringSubstitutor.replace(
                ": ${lng},${lat} / ${alt}",
                Map.of(
                        "lng", this.longitude,
                        "lat", this.latitude,
                        "alt", this.altitude
                )
        );
    }
}
