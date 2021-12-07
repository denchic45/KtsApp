package com.denchic45.kts.rx.bus;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class RxBusIconPicker {
    private static RxBusIconPicker instance;
    private PublishSubject<String> subject;

    public static RxBusIconPicker getInstance() {
        if (instance == null) {
            instance = new RxBusIconPicker();
        }
        return instance;
    }

    public void postSelectedIcon(String icon) {
        subject.onNext(icon);
    }

    public Observable<String> observeSelectedIcon() {
        if (subject == null)
            subject = PublishSubject.create();
        return subject;
    }

    public void completeEvent() {
        subject.onComplete();
        subject = null;
    }
}
