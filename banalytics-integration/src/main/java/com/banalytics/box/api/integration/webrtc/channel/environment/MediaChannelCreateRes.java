package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MediaChannelCreateRes extends AbstractChannelMessage {
    /**
     * channel name or empty if task is not AbstractStreamingMediaTask
     */
    private int streamId;

    public MediaChannelCreateRes() {
        super(MessageType.MEDIA_CHNL_CRT_RES);
    }
}
