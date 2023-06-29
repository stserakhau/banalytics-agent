package com.banalytics.box.api.integration;

public interface IMessage {
    MessageType getType();

    String toJson() throws Exception;
}
