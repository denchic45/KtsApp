package com.denchic45.kts.uivalidator.validatorlistener

import com.denchic45.kts.SingleLiveData

abstract class LiveDataValidationListener<T:Any?>(
    protected val mutableLiveData: SingleLiveData<T>
) : ValidationListener()