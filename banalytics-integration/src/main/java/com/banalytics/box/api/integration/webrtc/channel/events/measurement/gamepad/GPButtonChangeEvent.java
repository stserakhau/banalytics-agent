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
public class GPButtonChangeEvent extends AbstractEvent {
    @UIComponent(index = 5, type = ComponentType.drop_down, uiConfig = {
            @UIComponent.UIConfig(name = "sort", value = "asc"),
            @UIComponent.UIConfig(name = "api-uuid", value = "00000000-0000-0000-0000-000000000011"),
            @UIComponent.UIConfig(name = "api-method", value = "readGamepadsIds")
    })
    public String gamepadId;

    @UIComponent(index = 10, type = ComponentType.int_input, uiConfig = {
            @UIComponent.UIConfig(name = "min", value = "0"),
            @UIComponent.UIConfig(name = "max", value = "64")
    })
    public int buttonIndex;

    @UIComponent(index = 20, type = ComponentType.drop_down, uiConfig = {
            @UIComponent.UIConfig(name = "show-empty", value = "true"),
            @UIComponent.UIConfig(name = "possibleValues", value = "true~true;false~false")
    })
    public Boolean pressed;

    @UIComponent(index = 30, type = ComponentType.drop_down, uiConfig = {
            @UIComponent.UIConfig(name = "show-empty", value = "true"),
            @UIComponent.UIConfig(name = "possibleValues", value = "true~true;false~false")
    })
    public Boolean touched;

    public double value;

    public GPButtonChangeEvent() {
        super(MessageType.EVT_SYS_GAMEPAD_BTN);
    }

    public GPButtonChangeEvent(String gamepadId, int buttonIndex, boolean pressed, boolean touched, double value) {
        this();
        this.gamepadId = gamepadId;
        this.buttonIndex = buttonIndex;
        this.pressed = pressed;
        this.touched = touched;
        this.value = value;
    }
}
