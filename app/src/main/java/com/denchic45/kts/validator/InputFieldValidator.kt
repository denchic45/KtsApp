package com.denchic45.kts.validator

import androidx.lifecycle.MutableLiveData

class InputFieldValidator(
    condition: Boolean,
    inputErrorLiveData: MutableLiveData<Pair<Int, String?>>,
    errorValue: Pair<Int, String?>
) : Validator<Pair<Int, String?>>(condition, inputErrorLiveData, errorValue) {
    override fun removeError() {
        errorLiveData.value = Pair(errorValue!!.first,null )
    }
}