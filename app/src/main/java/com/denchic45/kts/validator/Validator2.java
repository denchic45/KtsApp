package com.denchic45.kts.validator;

import androidx.lifecycle.MutableLiveData;

public class Validator2<T> {
    protected final MutableLiveData<T> errorLiveData;
    protected final T errorValue;
    protected final T completeValue;
    private final boolean condition;


    public Validator2(boolean condition, MutableLiveData<T> errorLiveData, T errorValue, T completeValue) {
        this.condition = condition;
        this.errorLiveData = errorLiveData;
        this.errorValue = errorValue;
        this.completeValue = completeValue;
    }

    public Validator2(boolean condition, MutableLiveData<T> errorLiveData, T errorValue) {
        this.condition = condition;
        this.errorLiveData = errorLiveData;
        this.errorValue = errorValue;
        this.completeValue = null;
    }

    public void validate() {
        if (errorLiveData  == null) {
            return;
        }
        if (!condition) {
            errorLiveData.setValue(errorValue);
        } else {
            errorLiveData.setValue(completeValue);
        }
    }

    public boolean isValidate() {
        return condition;
    }

}
