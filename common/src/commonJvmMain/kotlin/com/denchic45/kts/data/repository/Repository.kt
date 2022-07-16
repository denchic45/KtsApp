package com.denchic45.kts.data.repository

import com.denchic45.kts.data.service.AppVersionService
import com.denchic45.kts.data.service.NetworkService
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.OldVersionException
import java.util.function.Consumer


abstract class Repository protected constructor() :
    PreconditionsRepository {
    val subscriptions: MutableMap<String, Subscription> = HashMap()


    protected fun addListenerRegistration(key: String, subscription: Subscription) {
        if (subscriptions.containsKey(key)) {
            removeListener(key)
            subscriptions.replace(key, subscription)
        } else {
            subscriptions[key] = subscription
        }
    }

    protected fun hasListener(key: String): Boolean {
        return subscriptions.containsKey(key)
    }


    protected fun addListenerRegistrationIfNotExist(key: String, registration: Subscription) {
        if (!subscriptions.containsKey(key)) subscriptions[key] = registration
    }

    fun removeListenerRegistration(key: String) {
        subscriptions[key]?.unsubscribe()
        subscriptions.remove(key)
    }

    open fun removeListeners() {
        subscriptions.values.forEach(Consumer { obj: Subscription -> obj.unsubscribe() })
    }

    fun removeListener(key: String) {
        subscriptions[key]!!.unsubscribe()
    }

    fun interface Subscription {
        fun unsubscribe()
    }

    class Subscription2<T>(val lambdaValue: () -> T, private val lambdaUnsubscribe: Subscription) {
        var value: T? = null
        fun subscribe() {
            value = lambdaValue()
        }

        fun unsubscribe() {
            lambdaUnsubscribe.unsubscribe()
        }
    }
}

interface PreconditionsRepository {

    val networkService: NetworkService
    val appVersionService: AppVersionService
//    val context: Context

    val isNetworkNotAvailable: Boolean
        get() = !networkService.isNetworkAvailable

//    fun isGoogleServicesAvailable(): Boolean {
//        return GoogleApiAvailability.getInstance()
//            .isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
//    }

    suspend fun requireAllowWriteData() {
        if (isNetworkNotAvailable) throw NetworkException()
        //TODO найти способ проверки на DEBUG версию
//        if (!BuildConfig.DEBUG) {
        if (appVersionService.isOldCurrentVersion()) throw OldVersionException()
//        }
    }

    fun requireNetworkAvailable() {
        if (isNetworkNotAvailable)
            throw NetworkException()
    }
}

