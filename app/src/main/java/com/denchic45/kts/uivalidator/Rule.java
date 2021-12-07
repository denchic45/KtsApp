package com.denchic45.kts.uivalidator;

import java.util.function.BooleanSupplier;

public class Rule {
    private final BooleanSupplier validate;
    private final String errorMessage;
    private final int errorResId;

    public Rule(BooleanSupplier validate, String errorMessage) {
        this.validate = validate;
        this.errorMessage = errorMessage;
        errorResId = 0;
    }

    public Rule(BooleanSupplier validate, int errorResId) {
        this.validate = validate;
        this.errorResId = errorResId;
        errorMessage = null;
    }

    public Rule(BooleanSupplier validate) {
        this.validate = validate;
        this.errorResId = 0;
        errorMessage = null;
    }

    public boolean isValid() {
        return validate.getAsBoolean();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorResId() {
        return errorResId;
    }
}