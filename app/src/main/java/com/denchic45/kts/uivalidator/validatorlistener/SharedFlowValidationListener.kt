package com.denchic45.kts.uivalidator.validatorlistener

import kotlinx.coroutines.flow.MutableSharedFlow

abstract class SharedFlowValidationListener<T>(protected val mutableSharedFlow: MutableSharedFlow<T>) :
    ValidationListener()