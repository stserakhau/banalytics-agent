package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingsGroupsRes extends AbstractChannelMessage {
    private List<ThingsGroup> groups;

    public ThingsGroupsRes() {
        super(MessageType.THNGS_GRPS_RES);
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    public static class ThingsGroup {
        public String className;

        public ThingsGroup() {
        }

        public ThingsGroup(String className) {
            this.className = className;
        }
    }
}
