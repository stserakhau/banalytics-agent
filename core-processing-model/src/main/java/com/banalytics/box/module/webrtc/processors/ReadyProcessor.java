package com.banalytics.box.module.webrtc.processors;

import com.banalytics.box.api.integration.webrtc.Offer;
import com.banalytics.box.module.webrtc.client.adaptor.CreateSessionDescriptionObserverAdaptor;
import com.banalytics.box.module.webrtc.client.adaptor.SetSessionDescriptionObserverAdaptor;
import dev.onvoid.webrtc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadyProcessor {
    public static Offer execute(RTCPeerConnection peerConnection) throws Exception {
        Holder<Offer> offerHolder = new Holder<>();
        peerConnection.createOffer(
                new RTCOfferOptions(),
                new CreateSessionDescriptionObserverAdaptor("Can't create offer: {}") {
                    @Override
                    public void onSuccess(RTCSessionDescription description) {
                        peerConnection.setLocalDescription(
                                description,
                                new SetSessionDescriptionObserverAdaptor("Can't set local description: {}") {
                                    @Override
                                    public void onSuccess() {
                                        offerHolder.set(new Offer(description.sdp));
                                    }
                                }
                        );
                    }
                }
        );

        return offerHolder.get();
    }
}
