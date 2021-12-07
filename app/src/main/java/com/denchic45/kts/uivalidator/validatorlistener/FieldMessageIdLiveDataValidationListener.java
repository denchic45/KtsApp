package com.denchic45.kts.uivalidator.validatorlistener;

import androidx.lifecycle.MutableLiveData;

import com.denchic45.kts.Pair;
import com.denchic45.kts.uivalidator.Rule;

public class FieldMessageIdLiveDataValidationListener extends LiveDataValidationListener<Pair<Integer, Integer>> {

    private final int id;

    public FieldMessageIdLiveDataValidationListener(int id, MutableLiveData<Pair<Integer, Integer>> mutableLiveData) {
        super(mutableLiveData);
        this.id = id;
    }

    @Override
    void onSuccess() {
        mutableLiveData.setValue(new Pair<>(id, null));
    }

    @Override
    void onError(Rule rule) {
        mutableLiveData.setValue(new Pair<>(id, rule.getErrorResId()));
    }
}
