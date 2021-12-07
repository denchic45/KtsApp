package com.denchic45.kts.ui.group.choiceOfCurator

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChoiceOfCuratorInteractor @Inject constructor(
    private val teacherRepository: TeacherRepository
) : Interactor() {

    private var selectTeacher = PublishSubject.create<User>()

    fun postSelectedCurator(o: User) {
        selectTeacher.onNext(o)
    }

    fun observeSelectedCurator(): Observable<User> {
        if (selectTeacher.hasComplete()) selectTeacher = PublishSubject.create()
        return selectTeacher
    }

    override fun removeListeners() {
        selectTeacher.onComplete()
    }

    fun findTeacherByTypedName(name: String): Flow<Resource2<List<User>>> {
        return teacherRepository.findByTypedName(name)
    }
}