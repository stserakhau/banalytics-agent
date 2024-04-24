package com.banalytics.box.api.integration.webrtc.channel.environment.auth;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.model.ComponentRelation;
import com.banalytics.box.api.integration.model.SharePermission;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class AuthenticationRes extends AbstractChannelMessage {
    private boolean authenticated;

    String token;

    Map<UUID, SharePermission> permissions;

    Map<Class<?>, Set<ComponentRelation>> componentRelations;

    public AuthenticationRes() {
        super(MessageType.AUTH_RES);
    }
}
