package com.denchic45.kts.ui.group.choiceOfCurator

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ChoiceOfCuratorInteractor @Inject constructor(
    private val teacherRepository: TeacherRepository
) : Interactor {

    private lateinit var continuation: CancellableContinuation<User>
//    private var selectTeacher = PublishSubject.create<User>()

    fun postSelectedCurator(o: User) {
//        selectTeacher.onNext(o)
        continuation.resume(o)
    }

//    fun observeSelectedCurator(): Observable<User> {
//        if (selectTeacher.hasComplete()) selectTeacher = PublishSubject.create()
//        return selectTeacher
//    }

    suspend fun awaitSelectTeacher(): User {
        return suspendCancellableCoroutine {
            this.continuation = it
        }
    }

    override fun removeListeners() {
//        selectTeacher.onComplete()
    }

    fun findTeacherByTypedName(name: String): Flow<List<User>> {
        return teacherRepository.findByTypedName(name)
    }
}