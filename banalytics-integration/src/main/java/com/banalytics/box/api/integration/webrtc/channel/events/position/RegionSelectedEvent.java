package com.banalytics.box.api.integration.webrtc.channel.events.position;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
public class RegionSelectedEvent extends AbstractEvent {
    public int x;
    public int y;
    public int width;
    public int height;

    public RegionSelectedEvent() {
        super(MessageType.EVT_REG_SEL);
    }

    @Override
    public String textView() {
        return super.textView() + StringSubstitutor.replace(
                ": ${x}, ${y} / ${w} x ${h}",
                Map.of(
                        "x", this.x,
                        "y", this.y,
                        "w", this.width,
                        "h", this.height
                )
        );
    }
}
