package com.banalytics.box.module.webrtc;

public enum ChannelType {
    /**
     * media stream with video and|or audio
     */
    COMPONENT_MEDIA,
    /**
     * Command Request/Response channel
     */
    COMPONENT_COMMAND,
    /**
     * Channel with the component state/info
     */
    COMPONENT_INFO_CHANNEL,

    /**
     * Channel for alerts on application layer
     */
    COMMON_ALERT
}
