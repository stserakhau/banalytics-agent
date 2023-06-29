package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter extends DateTimeConverter {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    @Override
    protected Class getDefaultType() {
        return LocalDateTime.class;
    }

    @Override
    protected Object convertToType(Class targetType, Object value) throws Exception {
        if (value instanceof String val) {
            try {
                long timestamp = Long.parseLong(val);
                return new Timestamp(timestamp).toLocalDateTime();
            } catch (NumberFormatException e) {
                return LocalDateTime.parse(val, DTF);
            }
        }
        return super.convertToType(targetType, value);
    }
}
