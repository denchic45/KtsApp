package com.denchic45.kts.validator;

import androidx.lifecycle.MutableLiveData;

public class SimpleValidator extends Validator<String>{
    public SimpleValidator(boolean condition, MutableLiveData<String> errorLiveData, String errorValue) {
        super(condition, errorLiveData, errorValue);
    }
}
