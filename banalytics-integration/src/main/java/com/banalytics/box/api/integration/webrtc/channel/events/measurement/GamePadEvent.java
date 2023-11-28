package com.banalytics.box.api.integration.webrtc.channel.events.measurement;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Gamepad_API
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GamePadEvent extends AbstractMessage {
    //https://developer.mozilla.org/en-US/docs/Web/API/Gamepad/axes
    public double[] axes;

    //https://developer.mozilla.org/en-US/docs/Web/API/Gamepad/buttons
    public double[] buttons;

//    https://developer.mozilla.org/en-US/docs/Web/API/GamepadPose
//    GamepadPose pose

    public GamePadEvent() {
        super(MessageType.EVT_SYS_GAMEPAD);
    }
}
