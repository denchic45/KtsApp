package com.denchic45.kts.uivalidator.util;

import com.denchic45.kts.utils.Validations;

import java.util.function.Supplier;

public class PhoneNumValid implements Supplier<Boolean> {

    private final String phoneNum;

    public PhoneNumValid(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    @Override
    public Boolean get() {
        return Validations.isValidPhoneNumber(phoneNum);
    }
}
