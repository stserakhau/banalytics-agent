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

    @UIComponent(index = 70, type = ComponentType.multi_select,uiConfig = {
            @UIComponent.UIConfig(name = "sort", value = "asc")
    })
    public KeyCode code;

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

    public enum KeyCode {
        ArrowLeft, ArrowUp, ArrowRight, ArrowDown,
        AltLeft, AltRight,
        Backquote,
        Backslash,
        Backspace,
        BracketLeft, BracketRight,
        CapsLock,
        Comma,
        ContextMenu,
        ControlLeft, ControlRight,
        Delete,
        Digit0, Digit1, Digit2, Digit3, Digit4, Digit5, Digit6, Digit7, Digit8, Digit9,
        End,
        Enter,
        Equal,
        Escape,
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
        Home,
        Insert,
        KeyA, KeyB, KeyC, KeyD, KeyE, KeyF, KeyG, KeyH, KeyI, KeyJ, KeyK, KeyL, KeyM,
        KeyN, KeyO, KeyP, KeyQ, KeyR, KeyS, KeyT, KeyU, KeyV, KeyW, KeyX, KeyY, KeyZ,
        MetaLeft, MetaRight,
        Minus,
        NumLock,
        Numpad0, Numpad1, Numpad2, Numpad3, Numpad4, Numpad5, Numpad6, Numpad7, Numpad8, Numpad9,
        NumpadMultiply, NumpadAdd, NumpadSubtract, NumpadDecimal, NumpadDivide,
        PageUp, PageDown,
        Pause,
        Period,
        PrintScreen,
        Quote,
        ScrollLock,
        Space,
        Tab,
        Semicolon,
        ShiftLeft, ShiftRight,
        Slash
    }
}