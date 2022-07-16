package com.denchic45.kts.util

import com.denchic45.kts.data.repository.Repository
import com.google.firebase.firestore.ListenerRegistration

fun Repository.addListenerRegistration(
    key: String,
    lambdaRegistration: () -> ListenerRegistration
) {
    val listenerRegistration = lambdaRegistration()
    if (subscriptions.containsKey(key)) {
        removeListener(key)
        subscriptions.replace(key) { listenerRegistration.remove() }
    } else {
        subscriptions[key] = Repository.Subscription { listenerRegistration.remove() }
    }
}

fun Repository.addListenerRegistrationIfNotExist(
    key: String,
    lambdaRegistration: () -> ListenerRegistration
) {
    if (!subscriptions.containsKey(key)) {
        val registration = lambdaRegistration()
        subscriptions[key] = Repository.Subscription { registration.remove() }
    }
}