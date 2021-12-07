package com.denchic45

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class CombinedLiveData<A, B>(ld1: LiveData<A>, ld2: LiveData<B>) :
    MediatorLiveData<Pair<A, B>>() {
    private var a: A? = null
    private var b: B? = null
    private var resultA = false
    private var resultB = false
    private fun trySetValue() {
        if (resultA && resultB) {
            value = a!! to b!!
            resultB = false
            resultA = resultB
        }
    }

    init {
        addSource(ld1) { a: A ->
            resultA = true
            this.a = a
            trySetValue()
        }
        addSource(ld2) { b: B ->
            resultB = true
            this.b = b
            trySetValue()
        }
    }
}