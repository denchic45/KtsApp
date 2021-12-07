package com.denchic45.kts.uivalidator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UIValidator {

    private final List<Validation> validations;

    public UIValidator() {
        validations = new ArrayList<>();
    }

    public UIValidator(List<Validation> validations) {
        this.validations = validations;
    }

    @Contract("_ -> new")
    public static <T> @NotNull UIValidator of(Validation... validations) {
        return new UIValidator(Arrays.asList(validations));
    }

    public void addValidation(Validation validation) {
        validations.add(validation);
    }

    public boolean runValidates() {
        validations.forEach(Validation::validate);
        return validations.stream().allMatch(Validation::validate);
    }

    public void runValidates(Runnable runnable) {
        if (runValidates())
            runnable.run();
    }

//    public boolean isValid() {
//        return validations.stream().allMatch(Validation::isValid);
//    }
}
