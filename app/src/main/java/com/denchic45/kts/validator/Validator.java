package com.denchic45.kts.validator;

import androidx.lifecycle.MutableLiveData;

public abstract class Validator<T> {
    protected final MutableLiveData<T> errorLiveData;
    protected final T errorValue;
    private final boolean condition;


    public Validator(boolean condition, MutableLiveData<T> errorLiveData, T errorValue) {
        this.condition = condition;
        this.errorLiveData = errorLiveData;
        this.errorValue = errorValue;

    }

    public void validate() {
        if (errorLiveData == null)
            return;
        if (!condition) {
            sendError();
        } else {
            removeError();
        }
    }

    public void sendError() {
        errorLiveData.setValue(errorValue);
    }

    public void removeError() {
        errorLiveData.setValue(null);
    }

    public boolean isValidate() {
        return condition;
    }

}
