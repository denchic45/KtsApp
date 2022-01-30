package com.denchic45.kts.utils

import android.util.Patterns

object Validations {
    fun validEmail(target: CharSequence?): Boolean {
        return !target.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun notValidEmail(target: CharSequence) = !validEmail(target)

    fun validPhoneNumber(target: CharSequence?): Boolean {
        return if (target == null || target.length < 6) {
            false
        } else {
            Patterns.PHONE.matcher(target).matches()
        }
    }
}