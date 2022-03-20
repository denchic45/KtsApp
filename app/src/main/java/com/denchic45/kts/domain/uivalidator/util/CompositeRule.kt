package com.denchic45.kts.domain.uivalidator.util

import com.denchic45.kts.domain.uivalidator.IRule

class CompositeRule<T : Any>(private vararg val rules: IRule<T>) : IRule<T> {

    override fun validate(value: T, messageCallback: (message: ErrorMessage) -> Unit): Boolean {
        return rules.all { rule -> rule.validate(value) { messageCallback(it) } }
    }

    override fun isValid(value: T): Boolean {
        return rules.all { rule -> rule.isValid(value) }
    }
}