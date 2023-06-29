package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class SoundEvent extends AbstractEvent {
    public UUID sourceThingUuid;
    public boolean debug;
    public double[] magnitude;
    public double[] magnitudeAverage;

    public SoundEvent() {
        super(MessageType.EVT_SOUND);
    }

    public SoundEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle,
                      UUID sourceThingUuid, boolean debug, double[] magnitude, double[] magnitudeAverage) {
        super(MessageType.EVT_SOUND, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.sourceThingUuid = sourceThingUuid;
        this.debug = debug;
        this.magnitude = magnitude;
        this.magnitudeAverage = magnitudeAverage;
    }
}
