package com.denchic45.kts.data

import android.content.Context
import com.denchic45.kts.data.Repository.Subscription
import com.denchic45.kts.utils.NetworkException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import java.util.function.Consumer

abstract class Repository protected constructor(context: Context?) {
    abstract val networkService: NetworkService
    private val subscriptions: MutableMap<String, Subscription> = HashMap()
    fun emmitErrorIfNetworkNotAvailable(emitter: Emitter<*>): Boolean {
        val isNotAvailable = !networkService.isNetworkAvailable
        if (isNotAvailable) emitter.onError(NetworkException())
        return isNotAvailable
    }

    fun emmitErrorIfNetworkNotAvailable(emitter: CompletableEmitter): Boolean {
        val isNotAvailable = !networkService.isNetworkAvailable
        if (isNotAvailable) emitter.onError(NetworkException())
        return isNotAvailable
    }

    val isNetworkNotAvailable: Boolean
        get() = !networkService.isNetworkAvailable

    protected fun checkInternetConnection() {
        if (isNetworkNotAvailable) throw NetworkException()
    }

    protected fun addListenerRegistration(key: String, lambdaRegistration: () -> ListenerRegistration) {
        val listenerRegistration = lambdaRegistration()
        if (subscriptions.containsKey(key)) {
            removeListener(key)
            subscriptions.replace(key) { listenerRegistration.remove() }
        } else {
            subscriptions[key] = Subscription { listenerRegistration.remove() }
        }
//        addListener(key, Subscription { registration().remove() })
    }

    protected fun addListenerDisposable(key: String, lambdaDisposable: ()-> Disposable) {
        val disposable = lambdaDisposable()
        if (subscriptions.containsKey(key)) {
            removeListener(key)
            subscriptions.replace(key) { disposable.dispose() }
        } else {
            subscriptions[key] = Subscription { disposable.dispose() }
        }
//        addListener(key, { disposable.dispose() } as Subscription)
    }

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

    protected fun addListenerRegistrationIfNotExist(key: String, lambdaRegistration: () -> ListenerRegistration) {
        if (!subscriptions.containsKey(key)) {
            val registration = lambdaRegistration()
            subscriptions[key] = Subscription{ registration.remove() }
        }
//        addListenerIfNotExist(key,Subscription { registration().remove() })
    }

    protected fun addListenerDisposableIfNotExist(key: String, lambdaDisposable: () -> Disposable) {
        if (!subscriptions.containsKey(key)) {
            val disposable = lambdaDisposable()
            subscriptions[key] = Subscription{ disposable.dispose() }
        }
//        addListenerIfNotExist(key,Subscription { registration().remove() })
    }

//    protected fun addListenerRegistrationIfNotExist(key: String, emitter: Disposable) {
//        addListenerIfNotExist(key, { emitter.dispose() } as Subscription)
//    }

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

    private fun removeListener(key: String) {
        subscriptions[key]!!.unsubscribe()
    }

    fun interface Subscription {
        fun unsubscribe()
    }

    class Subscription2<T>(val lambdaValue:()->T, private val lambdaUnsubscribe: Subscription) {
        var value:T? = null
        fun subscribe() {
            value = lambdaValue()
        }
        fun unsubscribe() {
            lambdaUnsubscribe.unsubscribe()
        }
    }

    fun timestampsIsNull(querySnapshot: QuerySnapshot):Boolean {
        return querySnapshot.documents.any { timestampIsNull(it) }
    }

    fun timestampIsNull(documentSnapshot: DocumentSnapshot):Boolean {
        return documentSnapshot.getTimestamp("timestamp") == null
    }

    fun timestampsNotNull(querySnapshot: QuerySnapshot):Boolean {
        return !timestampsIsNull(querySnapshot)
    }

    fun timestampNotNull(documentSnapshot: DocumentSnapshot):Boolean {
        return !timestampIsNull(documentSnapshot)
    }
}