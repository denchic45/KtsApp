package com.denchic45.kts.uivalidator.validatorlistener

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.validatorlistener.LiveDataValidationListener

class FieldMessageLiveDataValidationListener(
    private val id: Int,
    mutableLiveData: MutableLiveData<Pair<Int, String?>>
) : LiveDataValidationListener<Pair<Int, String?>>(mutableLiveData) {
    override fun onSuccess() {
        mutableLiveData.value = Pair<Int, String?>(id, null)
    }

    override fun onError(rule: Rule) {
        mutableLiveData.value = Pair(id, rule.errorMessage)
    }
}