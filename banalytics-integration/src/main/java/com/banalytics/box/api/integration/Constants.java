package com.banalytics.box.api.integration;

import java.util.Map;
import java.util.UUID;

public enum Constants {
    ENVIRONMENT_UUID("environmentUuid", UUID.class),
    ENVIRONMENT_HASH("environmentHash", UUID.class),
    ENVIRONMENT_OWNER_ID("environmentOwner", Long.class),
    ENVIRONMENT_OWNER_EMAIL("environmentOwnerEmail", Long.class),
    ENVIRONMENT_SHARED_WITH_ACCOUNT_IDS("environmentSharedWith", Long.class),

    PUBLIC_TOKEN("publicToken", String.class),
    PUBLIC_ACCESS_TOKEN("publicAccessToken", String.class),
    PUBLIC_TARGET_ENVIRONMENT("targetEnvironment", UUID.class);

    public String varName;
    public Class<?> varType;

    <T> Constants(String varName, Class<T> varType) {
        this.varName = varName;
        this.varType = varType;
    }

    public static <T> T get(Map<String, Object> attributes, Constants environmentUuid) {
        return (T) attributes.get(environmentUuid.varName);
    }
}
