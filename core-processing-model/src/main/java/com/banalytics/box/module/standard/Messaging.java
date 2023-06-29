package com.banalytics.box.module.standard;

import com.banalytics.box.api.integration.AbstractMessage;

public interface Messaging<PARENT_MESSAGE_TYPE extends AbstractMessage> {
    void sendMessage(PARENT_MESSAGE_TYPE message) throws Exception;
}
