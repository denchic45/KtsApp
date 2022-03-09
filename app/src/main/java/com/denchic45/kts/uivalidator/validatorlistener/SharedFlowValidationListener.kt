package com.denchic45.kts.uivalidator.validatorlistener

import kotlinx.coroutines.flow.MutableSharedFlow
import com.denchic45.kts.uivalidator.validatorlistener.ValidationListener

abstract class SharedFlowValidationListener<T>(protected val mutableSharedFlow: MutableSharedFlow<T>) :
    ValidationListener()