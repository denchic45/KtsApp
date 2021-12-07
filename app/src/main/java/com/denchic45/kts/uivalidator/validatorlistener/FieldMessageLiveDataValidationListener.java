package com.denchic45.kts.uivalidator.validatorlistener;

import androidx.lifecycle.MutableLiveData;

import com.denchic45.kts.Pair;
import com.denchic45.kts.uivalidator.Rule;

public class FieldMessageLiveDataValidationListener extends LiveDataValidationListener<Pair<Integer, String>> {

    private final int id;

    public FieldMessageLiveDataValidationListener(int id, MutableLiveData<Pair<Integer, String>> mutableLiveData) {
        super(mutableLiveData);
        this.id = id;
    }

    @Override
    void onSuccess() {
        mutableLiveData.setValue(new Pair<>(id, null));
    }

    @Override
    void onError(Rule rule) {
        mutableLiveData.setValue(new Pair<>(id, rule.getErrorMessage()));
    }
}
