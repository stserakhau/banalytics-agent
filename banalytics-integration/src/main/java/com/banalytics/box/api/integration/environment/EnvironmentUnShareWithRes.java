package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentUnShareWithRes extends AbstractEnvironmentShareMessage {


    public EnvironmentUnShareWithRes() {
        super(MessageType.ENV_UN_SHARE_WITH_RES);
    }
}
