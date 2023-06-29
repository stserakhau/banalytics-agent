package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends DateTimeConverter {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected Class getDefaultType() {
        return LocalDate.class;
    }

    @Override
    protected Object convertToType(Class targetType, Object value) throws Exception {
        if (value instanceof String val) {
            try {
                long timestamp = Long.parseLong(val);
                return new Timestamp(timestamp).toLocalDateTime().toLocalDate();
            } catch (NumberFormatException e) {
                return LocalDate.parse(val, DTF);
            }
        }
        return super.convertToType(targetType, value);
    }
}
