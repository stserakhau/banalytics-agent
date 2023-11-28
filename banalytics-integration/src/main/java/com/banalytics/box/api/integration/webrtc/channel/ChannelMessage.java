package com.banalytics.box.api.integration.webrtc.channel;

import com.banalytics.box.api.integration.IMessage;

public interface ChannelMessage extends IMessage {
    int getRequestId();

    boolean isAsyncAllowed();
}
