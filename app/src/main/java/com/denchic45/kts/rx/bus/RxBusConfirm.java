package com.denchic45.kts.rx.bus;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class RxBusConfirm {
    private static RxBusConfirm instance;
    private PublishSubject<Boolean> subject;

    public static RxBusConfirm getInstance() {
        if (instance == null) {
            instance = new RxBusConfirm();
        }
        return instance;
    }

    public void postEvent(Boolean o) {
        if (subject != null)
            subject.onNext(o);
    }

    public Observable<Boolean> getEvent() {
        if (subject == null)
            subject = PublishSubject.create();

        return subject;
    }

    public void completeEvent() {
        if (subject != null)
            subject.onComplete();
        subject = null;
    }
}