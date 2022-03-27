package com.denchic45.kts.uivalidator.validatorlistener;

import androidx.lifecycle.MutableLiveData;

public abstract class LiveDataValidationListener<T> extends ValidationListener {

    protected final MutableLiveData<T> mutableLiveData;

    public LiveDataValidationListener(MutableLiveData<T> mutableLiveData) {
        this.mutableLiveData = mutableLiveData;
    }
}
