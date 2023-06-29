package com.banalytics.box.api.integration.suc;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Event sends from Portal to Environment when user added / removed or set upgrade to modules
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SynchronizeSoftwareEvent extends AbstractSUCIntegrationMessage {
    public SynchronizeSoftwareEvent() {
        super(MessageType.EVT_SYNC_SOFT);
    }
}
