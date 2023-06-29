package com.banalytics.box.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.banalytics.box.api.integration.utils.CommonUtils.createObjectMapper;

@Configuration
public class ObjectMapperConfiguration {

    @Bean("objectMapper")
    public ObjectMapper getObjectMapper() {
        return createObjectMapper();
    }


}
