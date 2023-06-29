package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentShareWithRes extends AbstractEnvironmentShareMessage {
    ShareState state;

    public EnvironmentShareWithRes() {
        super(MessageType.ENV_SHARE_WITH_RES);
    }

    public EnvironmentShareWithRes(ShareState state) {
        super(MessageType.ENV_SHARE_WITH_RES);
        this.state = state;
    }

    public enum ShareState {
        SHARED, INVITED
    }
}
