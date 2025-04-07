package com.sep490.dasrsbackend.Util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Random;

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

    public String generateMatchCode() {
        // Tạo 3 số đầu tiên ngẫu nhiên trong khoảng 100 - 999
        Random random = new Random();
        int firstPart = random.nextInt(1000); // Random từ 100 đến 999

        // Lấy 3 số cuối từ thời gian hiện tại tính bằng mili giây
        long currentTimeMillis = System.currentTimeMillis();
        int secondPart = (int) (currentTimeMillis % 1000); // Lấy 3 chữ số cuối

        // Ghép lại 3 số đầu tiên và 3 số sau thành mã duy nhất
        return String.format("%03d%03d", firstPart, secondPart);
    }

    public String generateRandomPassword(int length) {
         final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
         final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
         final String DIGITS = "0123456789";
         final String ALL_CHARACTERS = LOWERCASE + UPPERCASE + DIGITS;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Đảm bảo có ít nhất một chữ cái in hoa, một chữ cái in thường và một số
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        // Điền các ký tự ngẫu nhiên còn lại
        for (int i = 3; i < length; i++) {
            password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        // Xáo trộn lại các ký tự trong password để đảm bảo tính ngẫu nhiên
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}
