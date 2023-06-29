package com.banalytics.box.module.webrtc.client.adaptor;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CreateSessionDescriptionObserverAdaptor implements CreateSessionDescriptionObserver {
    String failMessage;

    public CreateSessionDescriptionObserverAdaptor(String failMessage) {
        this.failMessage = failMessage;
    }

    @Override
    public void onFailure(String error) {
        log.error(failMessage, error);
    }
}
