package com.sep490.dasrsbackend.Util;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

@UtilityClass
public class DateUtil {
    public final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public final String DATE_FORMAT = "yyyy-MM-dd";

    public static String formatTimestamp(Date date) {
        return formatTimestamp(date, DEFAULT_DATE_FORMAT);
    }

    public static String formatTimestamp(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Chuyển từ ICT -> UTC
    public static Date convertICTtoUTC(Date ictDate) {
        return convertTimeZone(ictDate, "Asia/Ho_Chi_Minh", "UTC");
    }

    // Chuyển từ UTC -> ICT
    public static Date convertUTCtoICT(Date utcDate) {
        return convertTimeZone(utcDate, "UTC", "Asia/Ho_Chi_Minh");
    }

    // Hàm chuyển đổi giữa các múi giờ
    public static Date convertTimeZone(Date date, String fromTimeZone, String toTimeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
        String formattedDate = sdf.format(date);

        try {
            sdf.setTimeZone(TimeZone.getTimeZone(toTimeZone));
            return sdf.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getCurrentTimestamp() {
        return Date.from(ZonedDateTime.now().toInstant());
    }
}
