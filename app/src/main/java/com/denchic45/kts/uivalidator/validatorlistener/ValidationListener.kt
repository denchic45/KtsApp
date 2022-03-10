package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.uivalidator.Rule

abstract class ValidationListener {
    fun run(rule: Rule) {
        if (rule.isValid) onSuccess() else onError(rule)
    }

    abstract fun onSuccess()
    abstract fun onError(rule: Rule)

    companion object {
        val EMPTY: ValidationListener = object : ValidationListener() {
            override fun onSuccess() {}
            override fun onError(rule: Rule) {}
        }
    }
}