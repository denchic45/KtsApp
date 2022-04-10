package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.uivalidator.Rule

class FieldMessageIdLiveDataValidationListener(
    private val id: Int,
    mutableLiveData: SingleLiveData<Pair<Int, Int?>>
) : LiveDataValidationListener<Pair<Int, Int?>>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = Pair<Int, Int?>(id, null)
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = Pair(id, rule.errorResId)
    }
}