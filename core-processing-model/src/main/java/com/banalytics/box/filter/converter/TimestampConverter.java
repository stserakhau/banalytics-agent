package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.converters.DateTimeConverter;

import java.sql.Timestamp;
import java.util.Date;

public class TimestampConverter extends DateTimeConverter {
    @Override
    protected Class getDefaultType() {
        return Timestamp.class;
    }

    @Override
    protected Object convertToType(Class targetType, Object value) throws Exception {
        if (value instanceof String) {
            String strVal = (String) value;
            long val = 0;
            try {
                val = Long.parseLong(strVal);
            } catch (NumberFormatException e) {
                Date date = DateUtil.parseDate(strVal);
                val = date.getTime();
            }
            return new Timestamp(val);
        }
        return super.convertToType(targetType, value);
    }
}