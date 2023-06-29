package com.banalytics.box.api.integration.suc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentModuleUpdateStatusEvent extends AbstractSUCIntegrationMessage {
    UUID environmentUuid;

    Module module;

    ModuleUpdateStatus status;

    String statusMessage;

    public EnvironmentModuleUpdateStatusEvent() {
        super(MessageType.EVT_ENV_MOD_UPD_STS);
    }

    public EnvironmentModuleUpdateStatusEvent(UUID environmentUuid, Module module, ModuleUpdateStatus status) {
        this(environmentUuid, module, status, null);
    }

    public EnvironmentModuleUpdateStatusEvent(UUID environmentUuid, Module module, ModuleUpdateStatus status, String statusMessage) {
        this();
        this.environmentUuid = environmentUuid;
        this.module = module;
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public static EnvironmentModuleUpdateStatusEvent of(UUID environmentUuid, Module module, ModuleUpdateStatus status) {
        return new EnvironmentModuleUpdateStatusEvent(environmentUuid, module, status);
    }
    public static EnvironmentModuleUpdateStatusEvent of(UUID environmentUuid, Module module, ModuleUpdateStatus status, String message) {
        return new EnvironmentModuleUpdateStatusEvent(environmentUuid, module, status, message);
    }
}
