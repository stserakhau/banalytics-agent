package com.banalytics.box.api.integration.webrtc.channel.events.measurement;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/GravitySensor
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GravityEvent extends AbstractEvent implements ChannelMessage {
    public Double xAngular;
    public Double yAngular;
    public Double zAngular;

    public GravityEvent() {
        super(MessageType.EVT_SYS_GRAVITY);
    }

    @Override
    public int getRequestId() {
        return -1;
    }

    @Override
    public boolean isAsyncAllowed() {
        return true;
    }
}