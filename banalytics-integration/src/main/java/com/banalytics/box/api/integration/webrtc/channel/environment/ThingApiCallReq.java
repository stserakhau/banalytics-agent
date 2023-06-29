package com.banalytics.box.api.integration.webrtc.channel.environment;

import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString(callSuper = true)
public class ThingApiCallReq extends AbstractNodeRequest {
    private static final Set<String> ASYNC_CALL_DISABLED = Set.of("stream");

    public static final String PARAM_METHOD = "method";
    private long sequenceIdentifier;
    private Map<String, Object> params;

    public ThingApiCallReq() {
        super(MessageType.THNG_API_CALL_REQ);
    }

    @Override
    public boolean isAsyncAllowed() {
        return !ASYNC_CALL_DISABLED.contains(params.get(PARAM_METHOD));
    }
}
