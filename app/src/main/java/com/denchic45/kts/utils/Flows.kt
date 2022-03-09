package com.denchic45.kts.utils

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

inline fun <T> Flow<T>.collectWhenCreated(
    lifecycleScope: LifecycleCoroutineScope,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleScope.launchWhenCreated {
        this@collectWhenCreated.collect(action)
    }
}

inline fun <T> Flow<T>.collectWhenStarted(
    lifecycleScope: LifecycleCoroutineScope,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleScope.launchWhenStarted {
        this@collectWhenStarted.collect(action)
    }
}

inline fun <T> Flow<T>.collectWhenResumed(
    lifecycleScope: LifecycleCoroutineScope,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleScope.launchWhenResumed {
        this@collectWhenResumed.collect(action)
    }
}