package com.example.extra.Utils;

import org.springframework.stereotype.Component;

@Component
public final class NotificationUtils {
    public static String generateVerificationCode() {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            number.append(digit);
        }

        return number.toString();
    }

}
