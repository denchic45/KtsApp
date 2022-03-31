package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.uivalidator.Rule

class MessageIdLiveDataValidationListener(mutableLiveData: SingleLiveData<Int>) :
    LiveDataValidationListener<Int>(mutableLiveData) {
    override fun onSuccess() {
//        mutableLiveData.value = null
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = rule.errorResId
    }
}