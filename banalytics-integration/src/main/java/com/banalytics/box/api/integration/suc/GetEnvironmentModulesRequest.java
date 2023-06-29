package com.banalytics.box.api.integration.suc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class GetEnvironmentModulesRequest extends AbstractSUCIntegrationMessage {
    UUID environmentUUID;

    Module banalyticsBoxCoreModule;

    public GetEnvironmentModulesRequest() {
        super(MessageType.GET_ENV_MOD_REQ);
    }
}
