package com.denchic45.kts.domain.uivalidator

import com.denchic45.kts.domain.uivalidator.util.ErrorMessage

interface IRule<T> {
    fun validate(value: T, messageCallback: (message: ErrorMessage) -> Unit): Boolean

    fun isValid(value: T): Boolean
}