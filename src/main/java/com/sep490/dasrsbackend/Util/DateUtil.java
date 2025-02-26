package com.sep490.dasrsbackend.Util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class DateUtil {
    private final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String formatTimestamp(Date date) {
        return formatTimestamp(date, DEFAULT_DATE_FORMAT);
    }

    public static String formatTimestamp(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}
