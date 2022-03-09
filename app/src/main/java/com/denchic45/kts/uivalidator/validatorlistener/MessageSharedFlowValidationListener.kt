package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.uivalidator.Rule
import kotlinx.coroutines.flow.MutableSharedFlow

class MessageSharedFlowValidationListener(sharedFlow: MutableSharedFlow<String>) :
    SharedFlowValidationListener<String>(sharedFlow) {
    public override fun onSuccess() {
//        mutableSharedFlow.tryEmit(null)
    }

    public override fun onError(rule: Rule) {
        mutableSharedFlow.tryEmit(rule.errorMessage)
    }
}