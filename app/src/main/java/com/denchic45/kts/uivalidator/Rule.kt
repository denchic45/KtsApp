package com.denchic45.kts.uivalidator

import java.util.function.BooleanSupplier

class Rule {
    private val validate: BooleanSupplier
    val errorMessage: String?
    val errorResId: Int

    constructor(validate: BooleanSupplier, errorMessage: String?) {
        this.validate = validate
        this.errorMessage = errorMessage
        errorResId = 0
    }

    constructor(validate: BooleanSupplier, errorResId: Int) {
        this.validate = validate
        this.errorResId = errorResId
        errorMessage = null
    }

    constructor(validate: BooleanSupplier) {
        this.validate = validate
        errorResId = 0
        errorMessage = null
    }

    val isValid: Boolean
        get() = validate.asBoolean
}