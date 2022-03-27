package com.denchic45.kts.uivalidator.validatorlistener

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.uivalidator.Rule

class MessageLiveDataValidationListener(mutableLiveData: SingleLiveData<String?>) :
    LiveDataValidationListener<String?>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = null
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = rule.errorMessage
    }
}