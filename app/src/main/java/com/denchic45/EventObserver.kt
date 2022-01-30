package com.denchic45

import androidx.lifecycle.Observer

class EventObserver<T>(private val content: EventUnhandledContent<T>) : Observer<T> {
    override fun onChanged(event: T) {
        if (event != null) {
            content.onEvent(event)
        }
    }

    fun interface EventUnhandledContent<T> {
        fun onEvent(t: T)
    }
}