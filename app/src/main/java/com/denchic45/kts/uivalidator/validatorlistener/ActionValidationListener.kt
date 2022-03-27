package com.denchic45.kts.uivalidator.validatorlistener;

import androidx.annotation.NonNull;

import com.denchic45.kts.uivalidator.Rule;

import java.util.function.Consumer;

public class ActionValidationListener extends ValidationListener {

    private final Runnable successAction;
    private final Consumer<Rule> errorAction;

    public ActionValidationListener(Consumer<Rule> errorAction, Runnable successAction) {
        this.errorAction = errorAction;
        this.successAction = successAction;
    }

    @Override
    public void onSuccess() {
        successAction.run();
    }

    @Override
    public void onError(@NonNull Rule rule) {
        errorAction.accept(rule);
    }
}
