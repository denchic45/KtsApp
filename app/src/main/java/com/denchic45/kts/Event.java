package com.denchic45.kts;

public class Event<E extends Enum<E>, T> {

    private final Enum<E> eventType;
    private final T content;

    public Event(Enum<E> eventType, T content) {
        this.eventType = eventType;
        this.content = content;
    }

    public Enum<E> getEventType() {
        return eventType;
    }

    public T getContent() {
        return content;
    }
}
