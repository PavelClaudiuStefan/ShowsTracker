package com.pavelclaudiustefan.shadowapps.showstracker.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {}

    public static boolean isValidEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isFreeAccount(String email) {
        String[] adminAccounts = {"pavelclaudiu96@gmail.com",
                                  "pavel.claudiu_96@yahoo.com",
                                  "varvariciv@gmail.com"};

        if (email == null || email.isEmpty()) {
            return true;
        }

        for (String accountEmail : adminAccounts) {
            if (accountEmail.equals(email)) {
                return false;
            }
        }

        return true;
    }

}
