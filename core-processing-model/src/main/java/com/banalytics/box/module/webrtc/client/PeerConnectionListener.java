package com.banalytics.box.module.webrtc.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public interface PeerConnectionListener {
    default void onNew(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }


    default void onConnecting(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }

    default void onConnected(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }

    default void onClosed(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }

    default void onDisconnected(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }

    default void onFailed(PeerConnectionListenerAdaptor.ConnectionEvent event) {
    }

    @AllArgsConstructor
    @Getter
    class ConnectionEvent {
        final RTCClient rtcClient;

        public static ConnectionEvent of(RTCClient rtcClient) {
            return new ConnectionEvent(rtcClient);
        }

        @Override
        public String toString() {
            return "ConnectionEvent{" +
                    "connectedWithAgent=" + rtcClient.environmentUUID +
                    '}';
        }
    }
}
