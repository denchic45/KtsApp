package com.denchic45.kts.domain.uivalidator

interface Validatable {
    fun isValid(): Boolean

    fun validate():Boolean
}