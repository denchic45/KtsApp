package com.denchic45.kts.domain.uivalidator

class Validator(private vararg val conditions: Condition<out Any>) : Validatable {

    override fun validate(): Boolean {
        conditions.forEach(Condition<out Any>::validate)
        return isValid()
    }

    override fun isValid(): Boolean = conditions
        .all(Condition<out Any>::isValid)
}