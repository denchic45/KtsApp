package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.validatorlistener.ValidationListener
import java.util.function.Consumer

class ActionValidationListener(
    private val errorAction: Consumer<Rule>,
    private val successAction: Runnable
) : ValidationListener() {
    override fun onSuccess() {
        successAction.run()
    }

    override fun onError(rule: Rule) {
        errorAction.accept(rule)
    }
}