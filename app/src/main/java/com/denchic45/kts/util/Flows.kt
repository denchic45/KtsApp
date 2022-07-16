package com.denchic45.kts.util

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

fun <T> Flow<T>.collectWhenCreated(
    lifecycleScope: LifecycleCoroutineScope,
    action: FlowCollector<T> = FlowCollector {}
) {
    lifecycleScope.launchWhenCreated {
        this@collectWhenCreated.collect(action)
    }
}

fun <T> Flow<T>.collectWhenStarted(
    lifecycleScope: LifecycleCoroutineScope,
    action: FlowCollector<T> = FlowCollector {}
) {
    lifecycleScope.launchWhenStarted {
        this@collectWhenStarted.collect(action)
    }
}

fun <T> Flow<T>.collectWhenResumed(
    lifecycleScope: LifecycleCoroutineScope,
    action: FlowCollector<T> = FlowCollector {}
) {
    lifecycleScope.launchWhenResumed {
        this@collectWhenResumed.collect(action)
    }
}