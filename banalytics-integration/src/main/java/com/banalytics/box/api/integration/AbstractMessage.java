package com.banalytics.box.api.integration;

import com.banalytics.box.api.integration.utils.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractMessage implements IMessage {
    protected final MessageType type;

    public AbstractMessage(MessageType type) {
        this.type = type;
    }

    public static <T> T from(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = CommonUtils.DEFAULT_OBJECT_MAPPER;
        JsonNode tree = objectMapper.readTree(json);
        String type = tree.get("type").asText();
        return (T) objectMapper.readValue(json, MessageType.valueOf(type).clazz);
    }

    public String toJson() throws Exception {
        return CommonUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(this);
    }
}
