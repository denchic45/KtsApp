package com.denchic45.kts.utils

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

fun <T> Flow<T>.collectWhenCreated(
    lifecycleScope: LifecycleCoroutineScope,
    action: suspend (value: T) -> Unit = {}
) {
    lifecycleScope.launchWhenCreated {
        this@collectWhenCreated.collect(action)
    }
}

fun <T> Flow<T>.collectWhenStarted(
    lifecycleScope: LifecycleCoroutineScope,
    action: suspend (value: T) -> Unit = {}
) {
    lifecycleScope.launchWhenStarted {
        this@collectWhenStarted.collect(action)
    }
}

fun <T> Flow<T>.collectWhenResumed(
    lifecycleScope: LifecycleCoroutineScope,
    action: suspend (value: T) -> Unit = {}
) {
    lifecycleScope.launchWhenResumed {
        this@collectWhenResumed.collect(action)
    }
}