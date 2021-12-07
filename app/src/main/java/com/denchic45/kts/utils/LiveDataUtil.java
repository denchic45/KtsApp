package com.denchic45.kts.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

public class LiveDataUtil {

    public static <T> LiveData<T> observeOnce(final @NotNull LiveData<T> liveData, final Observer<T> observer) {
        liveData.observeForever(new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
        return liveData;
    }

}