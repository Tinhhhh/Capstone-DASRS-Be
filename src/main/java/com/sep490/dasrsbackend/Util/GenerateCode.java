package com.sep490.dasrsbackend.Util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.Month;

@UtilityClass
public class GenerateCode {

    public String seasonPrefix(LocalDateTime time) {
        StringBuilder StringBuilder = new StringBuilder();
        String year = String.valueOf(time.getYear()).substring(2);
        Month month = time.getMonth();

        String season;
        if (month.getValue() >= 3 && month.getValue() <= 5) {
            season = "SP"; //Spring
        } else if (month.getValue() >= 6 && month.getValue() <= 8) {
            season = "SM"; //Summer
        } else if (month.getValue() >= 9 && month.getValue() <= 11) {
            season = "FA"; //Fall
        } else {
            season = "WT"; //Winter
        }

        return StringBuilder.append(season).append(year).toString();

    }
}
