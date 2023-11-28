package com.banalytics.box.api.integration.webrtc.channel.events.measurement;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KeyboardEvent extends AbstractEvent implements ChannelMessage {
    @UIComponent(index = 10, type = ComponentType.drop_down)
    public PressType pressType;

    @UIComponent(index = 20, type = ComponentType.checkbox)
    public boolean altKey;
    @UIComponent(index = 30, type = ComponentType.checkbox)
    public boolean ctrlKey;
    @UIComponent(index = 40, type = ComponentType.checkbox)
    public boolean shiftKey;
    @UIComponent(index = 50, type = ComponentType.checkbox)
    public boolean metaKey;
    @UIComponent(index = 60, type = ComponentType.checkbox)
    public boolean repeat;

    @UIComponent(index = 70, type = ComponentType.text_input)
    public String code;
    @UIComponent(index = 80, type = ComponentType.text_input)
    public String key;

    @UIComponent(index = 90, type = ComponentType.text_input)
    public Integer location;

    public KeyboardEvent() {
        super(MessageType.EVT_SYS_KEYB);
    }

    public enum PressType {
        DOWN, UP
    }

    @Override
    public int getRequestId() {
        return -1;
    }

    @Override
    public boolean isAsyncAllowed() {
        return true;
    }
}