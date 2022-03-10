package com.denchic45.kts.uivalidator.validatorlistener

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.validatorlistener.LiveDataValidationListener

class FieldMessageIdLiveDataValidationListener(
    private val id: Int,
    mutableLiveData: MutableLiveData<Pair<Int, Int?>>
) : LiveDataValidationListener<Pair<Int, Int?>>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = Pair<Int, Int?>(id, null)
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = Pair(id, rule.errorResId)
    }
}