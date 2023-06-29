package com.banalytics.box.module.webrtc.client.adaptor;

import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SetSessionDescriptionObserverAdaptor implements SetSessionDescriptionObserver {
    String failMessage;

    public SetSessionDescriptionObserverAdaptor(String failMessage) {
        this.failMessage = failMessage;
    }

    @Override
    public void onFailure(String error) {
        log.error(failMessage, error);
    }
}
