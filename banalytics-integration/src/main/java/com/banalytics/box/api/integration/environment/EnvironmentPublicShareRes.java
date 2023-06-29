package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentPublicShareRes extends AbstractEnvironmentShareMessage {
    public EnvironmentPublicShareRes() {
        super(MessageType.ENV_PUB_SHARE_RES);
    }
}
