package com.denchic45.kts.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

object LiveDataUtils {
    fun <T> observeOnce(liveData: LiveData<T>, observer: Observer<T>): LiveData<T> {
        liveData.observeForever(object : Observer<T> {
            override fun onChanged(t: T) {
                liveData.removeObserver(this)
                observer.onChanged(t)
            }
        })
        return liveData
    }
}

//var <T> MutableLiveData<T>.postValue: T
//    get() = throw IllegalAccessError("This property use for posting value!")
//    set(value) {
//        postValue(value)
//    }

