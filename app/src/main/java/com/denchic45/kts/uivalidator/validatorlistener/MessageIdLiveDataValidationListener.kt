package com.denchic45.kts.uivalidator.validatorlistener

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.validatorlistener.LiveDataValidationListener

class MessageIdLiveDataValidationListener(mutableLiveData: MutableLiveData<Int>) :
    LiveDataValidationListener<Int>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = null
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = rule.errorResId
    }
}