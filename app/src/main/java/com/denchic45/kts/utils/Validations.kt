package com.denchic45.kts.utils;

import android.text.TextUtils;

public class Validations {

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || target.length() < 6) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }

    }
}
