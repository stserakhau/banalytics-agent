package com.banalytics.box.api.integration.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.banalytics.box.api.integration.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public class CommonUtils {
    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = createObjectMapper();

    public static void sendMessage(WebSocketSession session, IMessage message) throws IOException {
        synchronized (session) {
            String json = DEFAULT_OBJECT_MAPPER.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.debug("Sending message:\n{}", json);
        }
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }

    public static int sdpExtractMaxMessageSize(String sdp) {
        String[] lines = sdp.split("\n");
        for (String line : lines) {
            if(line.startsWith("a=max-message-size:")) {
                String val = line.substring(19).trim();
                return Integer.parseInt(val);
            }
        }
        return 16384;
    }
}
