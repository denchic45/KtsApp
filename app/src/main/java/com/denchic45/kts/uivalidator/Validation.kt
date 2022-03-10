package com.denchic45.kts.uivalidator

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.uivalidator.validatorlistener.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.function.Consumer

class Validation(private vararg val rules: Rule) {
    private var errorRunnable: Runnable? = null
    private var messageConsumer: Consumer<String>? = null
    private var resIdConsumer: Consumer<Int>? = null
    private var successRunnable: Runnable? = null
    private var runResetLiveData: Runnable? = null
    private var validationListener = ValidationListener.EMPTY
    fun validate(): Boolean {
        return isValid
    }

    fun onErrorRun(runnable: Runnable): Validation {
        errorRunnable = runnable
        return this
    }

    fun onErrorSendMessage(consumer: Consumer<String>): Validation {
        messageConsumer = consumer
        return this
    }

    fun onErrorSendMessage(fieldResId: Int, consumer: Consumer<String>): Validation {
        messageConsumer = consumer
        return this
    }

    fun onErrorSendIdRes(consumer: Consumer<Int>): Validation {
        resIdConsumer = consumer
        return this
    }

    fun onSuccessRun(runnable: Runnable): Validation {
        successRunnable = runnable
        return this
    }

    fun onSuccessResetMessage(liveData: SingleLiveData<String?>): Validation {
        runResetLiveData = Runnable { liveData.setValue(null) }
        return this
    }

    fun onSuccessResetResId(liveData: SingleLiveData<Int?>): Validation {
        runResetLiveData = Runnable { liveData.setValue(null) }
        return this
    }

    fun onSuccessResetMessage(
        liveData: SingleLiveData<Pair<Int, String?>>,
        fieldResId: Int
    ): Validation {
        runResetLiveData = Runnable { liveData.setValue(Pair<Int, String?>(fieldResId, null)) }
        return this
    }

    fun onSuccessResetResId(
        liveData: SingleLiveData<Pair<Int, Int?>>,
        fieldResId: Int
    ): Validation {
        runResetLiveData = Runnable { liveData.setValue(Pair<Int, Int?>(fieldResId, null)) }
        return this
    }

    fun sendMessageResult(validationListener: ValidationListener): Validation {
        this.validationListener = validationListener
        return this
    }

    fun sendMessageResult(mutableLiveData: MutableLiveData<String?>): Validation {
        validationListener = MessageLiveDataValidationListener(mutableLiveData)
        return this
    }

    fun sendMessageIdResult(
        id: Int,
        mutableLiveData: MutableLiveData<Pair<Int, Int?>>
    ): Validation {
        validationListener = FieldMessageIdLiveDataValidationListener(id, mutableLiveData)
        return this
    }

    fun sendMessageResult(
        id: Int,
        mutableLiveData: MutableLiveData<Pair<Int, String?>>
    ): Validation {
        validationListener = FieldMessageLiveDataValidationListener(id, mutableLiveData)
        return this
    }

    fun sendMessageIdResult(mutableLiveData: MutableLiveData<Int>): Validation {
        validationListener = MessageIdLiveDataValidationListener(mutableLiveData)
        return this
    }

    fun sendMessageIdResult(mutableSharedFlow: MutableSharedFlow<Int>): Validation {
        validationListener = MessageIdSharedFlowValidationListener(mutableSharedFlow)
        return this
    }

    fun sendActionResult(errorAction: Consumer<Rule?>?, successAction: Runnable?): Validation {
        validationListener = ActionValidationListener(errorAction, successAction)
        return this
    }

    fun sendMessageResult(sharedFlow: MutableSharedFlow<String>): Validation {
        validationListener = MessageSharedFlowValidationListener(sharedFlow)
        return this
    }

    private val isValid: Boolean
        private get() {
            for (rule in rules) {
                validationListener.run(rule)
                if (!rule.isValid) {
                    onPostError(rule)
                    return false
                }
            }
            onPostSuccess()
            return true
        }

    protected fun onPostSuccess() {
        if (successRunnable != null) successRunnable!!.run()
        if (runResetLiveData != null) runResetLiveData!!.run()
    }

    protected fun onPostError(rule: Rule) {
        if (errorRunnable != null) errorRunnable!!.run()
        if (messageConsumer != null) messageConsumer!!.accept(rule.errorMessage)
        if (resIdConsumer != null) resIdConsumer!!.accept(rule.errorResId)
    }

}