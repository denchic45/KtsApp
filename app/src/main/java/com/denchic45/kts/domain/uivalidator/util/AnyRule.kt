package com.denchic45.kts.domain.uivalidator.util

import com.denchic45.kts.domain.uivalidator.IRule

class AnyRule<T : Any>(
    private vararg val rules: IRule<T>,
    private val message: () -> ErrorMessage
) : IRule<T> {

    override fun validate(value: T, messageCallback: (message: ErrorMessage) -> Unit): Boolean {
        return rules
            .any { rule -> rule.isValid(value) }
            .apply { if (!this) messageCallback(message()) }
    }

    override fun isValid(value: T): Boolean {
        return rules.any { rule -> rule.isValid(value) }
    }
}