package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class MediaChannelCreateReq extends AbstractNodeRequest {
    private UUID taskUuid;
    private int streamId;
    private int requestedWidth;
    private int requestedHeight;
    private boolean requestedAudio;

    public MediaChannelCreateReq() {
        super(MessageType.MEDIA_CHNL_CRT_REQ);
    }

    @Override
    public boolean isAsyncAllowed() {
        return false;
    }
}
