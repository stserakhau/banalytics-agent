package com.banalytics.box.module.webrtc.client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PeerConnectionListenerAdaptor implements PeerConnectionListener {

    public void onNew(ConnectionEvent event) {
        log.info("New peer connection: {}", event);
    }


    public void onConnecting(ConnectionEvent event) {
        log.info("Peer connection connecting: {}", event);
    }

    public void onConnected(ConnectionEvent event) {
        log.info("Peer connection connected: {}", event);
    }

    public void onClosed(ConnectionEvent event) {
        log.info("Peer connection closed: {}", event);
    }

    public void onDisconnected(ConnectionEvent event) {
        log.info("Peer connection disconnected: {}", event);
    }

    public void onFailed(ConnectionEvent event) {
        RTCClient c = event.getRtcClient();
        log.info(
                "Peer connection failed: {} / {} / {} / {}",
                c.peerConnection.getConnectionState(),
                c.peerConnection.getIceConnectionState(),
                c.peerConnection.getSignalingState(),
                c.peerConnection.getIceGatheringState()
        );
    }


}
