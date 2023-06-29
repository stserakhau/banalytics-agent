package com.banalytics.box.api.integration.webrtc.channel;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ExceptionMessage extends AbstractChannelMessage {
    String message;

    Object[] args;

    StackTraceElement[] stackTrace;

    public ExceptionMessage() {
        super(MessageType.ERROR);
    }
}
