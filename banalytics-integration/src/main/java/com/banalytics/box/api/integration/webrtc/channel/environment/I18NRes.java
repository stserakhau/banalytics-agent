package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class I18NRes extends AbstractChannelMessage {
    private Map<UUID, Map<String, String>> uuidClassTitleMap;

    /**
     * [Lang -> Map[key->value]]
     */
    private Map<String, Map<String, String>> i18n;

    public I18NRes() {
        super(MessageType.I18N_RES);
    }
}
