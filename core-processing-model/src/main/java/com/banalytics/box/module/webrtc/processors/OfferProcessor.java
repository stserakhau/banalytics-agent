package com.banalytics.box.module.webrtc.processors;

import com.banalytics.box.api.integration.webrtc.Answer;
import com.banalytics.box.api.integration.webrtc.Offer;
import com.banalytics.box.module.webrtc.client.adaptor.CreateSessionDescriptionObserverAdaptor;
import com.banalytics.box.module.webrtc.client.adaptor.SetSessionDescriptionObserverAdaptor;
import dev.onvoid.webrtc.RTCAnswerOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OfferProcessor {
    public static Answer execute(RTCPeerConnection peerConnection, Offer offer) throws Exception {
        Holder<Answer> answerHolder = new Holder<>();
        peerConnection.setRemoteDescription(
                new RTCSessionDescription(
                        RTCSdpType.OFFER, offer.sdp
                ), new SetSessionDescriptionObserverAdaptor("Error set offer: {}") {
                    @Override
                    public void onSuccess() {
                        peerConnection.createAnswer(
                                new RTCAnswerOptions(),
                                new CreateSessionDescriptionObserverAdaptor("Error set answer: {}") {
                                    @Override
                                    public void onSuccess(RTCSessionDescription description) {
                                        peerConnection.setLocalDescription(
                                                description,
                                                new SetSessionDescriptionObserverAdaptor("Error set local description: {}") {
                                                    @Override
                                                    public void onSuccess() {
                                                        answerHolder.set(new Answer(description.sdp));
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                    }
                }
        );

        return answerHolder.get();
    }
}
