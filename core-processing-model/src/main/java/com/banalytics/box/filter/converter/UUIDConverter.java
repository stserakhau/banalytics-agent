package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.Converter;

import java.util.UUID;

public class UUIDConverter implements Converter {
    @Override
    public Object convert(Class type, Object value) {
        return UUID.fromString((String) value);
    }
}