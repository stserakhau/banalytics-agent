package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class EnvironmentStatusMessage extends AbstractMessage {
    private UUID environmentUuid;
    private EnvironmentStatusType status;
    private LocalDateTime updated;

    public EnvironmentStatusMessage() {
        super(MessageType.ENV_STATUS_MSG);
    }

    public EnvironmentStatusMessage(UUID environmentUuid, EnvironmentStatusType status, LocalDateTime updated) {
        this();
        this.environmentUuid = environmentUuid;
        this.status = status;
        this.updated = updated;
    }
}
