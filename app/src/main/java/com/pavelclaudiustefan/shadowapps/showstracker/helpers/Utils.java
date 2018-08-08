package com.pavelclaudiustefan.shadowapps.showstracker.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {}

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
