package com.banalytics.box.api.integration.webrtc.channel.events.measurement;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.ChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/GeolocationCoordinates
 */
@Getter
@Setter
@ToString(callSuper = true)
@Deprecated
public class GPSEvent extends AbstractEvent implements ChannelMessage {
    public Double latitude;
    public Double longitude;
    public Double accuracy;
    public Double altitude;
    public Double altitudeAccuracy;

    public GPSEvent() {
        super(MessageType.EVT_SYS_GPS);
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
