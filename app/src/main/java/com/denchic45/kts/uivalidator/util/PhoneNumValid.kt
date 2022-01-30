package com.denchic45.kts.uivalidator.util

import com.denchic45.kts.utils.Validations.validPhoneNumber
import com.denchic45.kts.utils.Validations
import java.util.function.Supplier

class PhoneNumValid(private val phoneNum: String) : Supplier<Boolean> {
    override fun get(): Boolean {
        return validPhoneNumber(phoneNum)
    }
}