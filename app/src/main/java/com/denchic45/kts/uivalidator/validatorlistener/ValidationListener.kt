package com.denchic45.kts.uivalidator.validatorlistener;

import com.denchic45.kts.uivalidator.Rule;

public abstract class ValidationListener {

    public static final ValidationListener EMPTY = new ValidationListener() {
        @Override
        void onSuccess() {
        }

        @Override
        void onError(Rule rule) {
        }
    };

    public final void run(Rule rule) {
        if (rule.isValid())
            onSuccess();
        else
            onError(rule);
    }

    abstract void onSuccess();

    abstract void onError(Rule rule);
}
