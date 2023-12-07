package com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class GPAxisChangeEvent extends AbstractEvent {
    @UIComponent(index = 10, type = ComponentType.int_input, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "64")
    })
    public int axisIndex;

    public double value;

    public GPAxisChangeEvent() {
        super(MessageType.EVT_SYS_GAMEPAD_AXS);
    }

    public GPAxisChangeEvent(int axisIndex, double value) {
        this();
        this.axisIndex = axisIndex;
        this.value = value;
    }
}
