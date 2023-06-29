package com.banalytics.box.api.integration;

import org.springframework.web.socket.WebSocketSession;

public interface MessageHandler<PARENT_MESSAGE extends IMessage> {
    /**
     * Processes received message.<br>
     *
     * If returns null it means that response to the caller not need, otherwise need to send returned message
     */
    PARENT_MESSAGE handleMessage(WebSocketSession session, AbstractMessage message)  throws Exception ;

    boolean isSupport(AbstractMessage message);
}
