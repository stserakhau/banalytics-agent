package com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Gamepad_API
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GamePadStateChangedEvent extends AbstractEvent implements ChannelMessage {
    public String gamepadId;

    //https://developer.mozilla.org/en-US/docs/Web/API/Gamepad/axes
    public double[] axes;

    //https://developer.mozilla.org/en-US/docs/Web/API/Gamepad/buttons
    public GamepadButton[] buttons;

//    https://developer.mozilla.org/en-US/docs/Web/API/GamepadPose
//    GamepadPose pose

    public GamePadStateChangedEvent() {
        super(MessageType.EVT_SYS_GAMEPAD_SCH);
    }

    @Override
    public int getRequestId() {
        return -1;
    }

    @Override
    public boolean isAsyncAllowed() {
        return true;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class GamepadButton {
        public int index;
        public boolean pressed;
        public boolean touched;
        public double value;
    }
}