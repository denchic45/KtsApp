package com.denchic45.kts.ui.iconPicker

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPickerInteractor @Inject constructor() {
    private var subject: PublishSubject<String>? = null
    fun postSelectedIcon(icon: String) {
        subject!!.onNext(icon)
    }

    fun observeSelectedIcon(): Observable<String>? {
        if (subject == null) subject = PublishSubject.create()
        return subject
    }
}