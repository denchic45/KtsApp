package com.denchic45.kts.uivalidator.validatorlistener;

import androidx.lifecycle.MutableLiveData;

import com.denchic45.kts.uivalidator.Rule;

public class MessageLiveDataValidationListener extends LiveDataValidationListener<String> {
    public MessageLiveDataValidationListener(MutableLiveData<String> mutableLiveData) {
        super(mutableLiveData);
    }

    @Override
    void onSuccess() {
        mutableLiveData.setValue(null);
    }

    @Override
    void onError(Rule rule) {
        mutableLiveData.setValue(rule.getErrorMessage());
    }
}
