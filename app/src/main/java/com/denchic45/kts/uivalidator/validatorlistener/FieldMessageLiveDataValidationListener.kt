package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.uivalidator.Rule

class FieldMessageLiveDataValidationListener(
    private val id: Int,
    mutableLiveData: SingleLiveData<Pair<Int, String?>>
) : LiveDataValidationListener<Pair<Int, String?>>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = Pair<Int, String?>(id, null)
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = Pair(id, rule.errorMessage)
    }
}