package com.denchic45.kts.ui.adminPanel.timetableEditor

import com.denchic45.kts.data.Interactor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class TimetableEditorInteractor : Interactor {
    private val showLessonsSubject = PublishSubject.create<Pair<Date, String>>()
    fun postShowLessons(dateWithGroupIdPair: Pair<Date, String>) {
        showLessonsSubject.onNext(dateWithGroupIdPair)
    }

    val showLessons: Observable<Pair<Date, String>>
        get() = showLessonsSubject

    override fun removeListeners() {}
}