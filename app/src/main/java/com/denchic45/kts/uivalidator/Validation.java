package com.denchic45.kts.uivalidator;

import androidx.lifecycle.MutableLiveData;

import com.denchic45.kts.SingleLiveData;
import com.denchic45.kts.uivalidator.validatorlistener.ActionValidationListener;
import com.denchic45.kts.uivalidator.validatorlistener.FieldMessageIdLiveDataValidationListener;
import com.denchic45.kts.uivalidator.validatorlistener.FieldMessageLiveDataValidationListener;
import com.denchic45.kts.uivalidator.validatorlistener.MessageIdLiveDataValidationListener;
import com.denchic45.kts.uivalidator.validatorlistener.MessageLiveDataValidationListener;
import com.denchic45.kts.uivalidator.validatorlistener.ValidationListener;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import kotlin.Pair;

public class Validation {

    private final Rule[] rules;
    private Runnable errorRunnable;
    private Consumer<String> messageConsumer;
    private Consumer<Integer> resIdConsumer;
    private Runnable successRunnable;
    private Runnable runResetLiveData;

    private ValidationListener validationListener = ValidationListener.EMPTY;

    public Validation(Rule... rules) {
        this.rules = rules;
    }


    public boolean validate() {
        return isValid();
    }

    public Validation onErrorRun(Runnable runnable) {
        errorRunnable = runnable;
        return this;
    }

    public Validation onErrorSendMessage(Consumer<String> consumer) {
        messageConsumer = consumer;
        return this;
    }

    public Validation onErrorSendMessage(int fieldResId, Consumer<String> consumer) {
        messageConsumer = consumer;
        return this;
    }

    public Validation onErrorSendIdRes(Consumer<Integer> consumer) {
        resIdConsumer = consumer;
        return this;
    }

    public Validation onSuccessRun(Runnable runnable) {
        successRunnable = runnable;
        return this;
    }

    public Validation onSuccessResetMessage(@NotNull SingleLiveData<String> liveData) {
        runResetLiveData = () -> liveData.setValue(null);
        return this;
    }

    public Validation onSuccessResetResId(@NotNull SingleLiveData<Integer> liveData) {
        runResetLiveData = () -> liveData.setValue(null);
        return this;
    }

    public Validation onSuccessResetMessage(@NotNull SingleLiveData<Pair<Integer, String>> liveData, int fieldResId) {
        runResetLiveData = () -> liveData.setValue(new Pair<>(fieldResId, null));
        return this;
    }

    public Validation onSuccessResetResId(@NotNull SingleLiveData<Pair<Integer, Integer>> liveData, int fieldResId) {
        runResetLiveData = () -> liveData.setValue(new Pair<>(fieldResId, null));
        return this;
    }

    public Validation sendMessageResult(ValidationListener validationListener) {
        this.validationListener = validationListener;
        return this;
    }

    public Validation sendMessageResult(MutableLiveData<String> mutableLiveData) {
        validationListener = new MessageLiveDataValidationListener(mutableLiveData);
        return this;
    }

    public Validation sendMessageResult(int id, MutableLiveData<Pair<Integer, String>> mutableLiveData) {
        validationListener = new FieldMessageLiveDataValidationListener(id, mutableLiveData);
        return this;
    }

    public Validation sendMessageIdResult(int id, MutableLiveData<Pair<Integer, Integer>> mutableLiveData) {
        validationListener = new FieldMessageIdLiveDataValidationListener(id, mutableLiveData);
        return this;
    }

    public Validation sendMessageIdResult(MutableLiveData<Integer> mutableLiveData) {
        validationListener = new MessageIdLiveDataValidationListener(mutableLiveData);
        return this;
    }

    public Validation sendActionResult(Consumer<Rule> errorAction, Runnable successAction) {
        validationListener = new ActionValidationListener(errorAction, successAction);
        return this;
    }

    private boolean isValid() {
        for (Rule rule : rules) {
            validationListener.run(rule);
            if (!rule.isValid()) {
                onPostError(rule);
                return false;
            }
        }
        onPostSuccess();
        return true;
    }

    protected void onPostSuccess() {
        if (successRunnable != null)
            successRunnable.run();
        if (runResetLiveData != null)
            runResetLiveData.run();
    }

    protected void onPostError(Rule rule) {
        if (errorRunnable != null)
            errorRunnable.run();
        if (messageConsumer != null)
            messageConsumer.accept(rule.getErrorMessage());
        if (resIdConsumer != null)
            resIdConsumer.accept(rule.getErrorResId());
    }


}
