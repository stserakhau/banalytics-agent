package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.Converter;

public class EnumConverter implements Converter {
    @Override
    public Object convert(Class type, Object value) {
        if (value instanceof Enum) {
            return Enum.valueOf(type, ((Enum) value).name());
        } else {
            return value == null ? null : Enum.valueOf(type, String.valueOf(value));
        }
    }
}