package com.denchic45.kts.uivalidator

import org.jetbrains.annotations.Contract
import java.util.function.Consumer

class UIValidator {
    private val validations: MutableList<Validation>

    constructor() {
        validations = ArrayList()
    }

    constructor(validations: MutableList<Validation>) {
        this.validations = validations
    }

    fun addValidation(validation: Validation) {
        validations.add(validation)
    }

    fun runValidates(): Boolean {
        validations.forEach(Consumer { obj: Validation -> obj.validate() })
        return validations.all { obj: Validation -> obj.validate() }
    }

    fun runValidates(runnable: Runnable) {
        if (runValidates()) runnable.run()
    }

    companion object {
        fun of(vararg validations: Validation): UIValidator {
            return UIValidator(validations.toMutableList())
        }
    }
}