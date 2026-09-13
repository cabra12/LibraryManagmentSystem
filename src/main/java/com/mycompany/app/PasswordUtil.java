package com.mycompany.app;

import java.security.SecureRandom;

public class PasswordUtil {
        public static String generateTempPassword() {
        String allowedChars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom(); //better for security than Math.random()
        StringBuilder sb = new StringBuilder();
        //Strings are usually immutiable, StringBuilder allows you to make something String-like that's mutable
        for (int i = 0; i < 12; i++) {
            sb.append(allowedChars.charAt(random.nextInt(allowedChars.length())));
        }
        return sb.toString();
        //turn it into a string
    }
}
