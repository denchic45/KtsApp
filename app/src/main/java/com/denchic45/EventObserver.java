package com.denchic45;

import androidx.lifecycle.Observer;

public class EventObserver<T> implements Observer<T> {

    private final EventUnhandledContent<T> content;

    public EventObserver(EventUnhandledContent<T> content) {
        this.content = content;
    }

    @Override
    public void onChanged(T event) {
        if (event != null) {
            content.onEvent(event);
        }
    }

    public interface EventUnhandledContent<T> {
        void onEvent(T t);
    }
}