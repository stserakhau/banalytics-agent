package com.banalytics.box.filter.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static Date parseDate(String date) {
        SimpleDateFormat dateFormat;
        if (date.toUpperCase().contains(" AM") || date.toUpperCase().contains(" PM")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a");
        } else if (date.toUpperCase().contains("AM") || date.toUpperCase().contains("PM")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd KK:mm:ssa");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        try {
            date = date.length() == 10 ? date + " 00:00:00" : date;
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
