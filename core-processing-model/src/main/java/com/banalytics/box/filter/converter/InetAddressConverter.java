package com.banalytics.box.filter.converter;

import org.apache.commons.beanutils.Converter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressConverter implements Converter {
    @Override
    public Object convert(Class type, Object value) {
        try {
            if (type == InetAddress.class) {
                return InetAddress.getByName((String) value);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
}
