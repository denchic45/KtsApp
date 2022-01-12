package com.denchic45.kts.ui.login.verifyPhoneNum

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.repository.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class VerifyPhoneNumInteractor @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val groupInfoRepository: GroupInfoRepository
) : Interactor {

    fun sendUserPhoneNumber(phoneNum: String): Observable<Resource<String>> {
        return Observable.create { emitter: ObservableEmitter<Resource<String>> ->
            authRepository.sendUserPhoneNumber(phoneNum).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ value: Resource<String> ->
                    emitter.onNext(
                        value
                    )
                }, { error: Throwable -> emitter.onError(error) })
        }
    }

    //    private @NonNull Completable loadData(@NotNull User user) {
    //        Set<Completable> completableList = new HashSet<>();
    //        String groupUuid = user.getGroupUuid();
    //        if (user.isTeacher()) {
    //            completableList.add(groupInfoRepository.loadSubjectsAndGroupsByTeacher(user));
    //            completableList.add(lessonRepository.loadLessonsByTeacherUuidAndStartDate(user.getUuid(), getFirstDayOfLastWeek()));
    //        }
    //        if (user.hasGroup()) {
    //            completableList.add(groupRepository.loadGroupPreference(groupUuid));
    //            completableList.add(lessonRepository.loadLessonsInDateRange(getFirstDayOfLastWeek()));
    //            if (user.isCurator()) {
    //                groupInfoRepository.getGroupInfoByUuid(groupUuid);
    //            }
    //        }
    //        completableList.add(subjectRepository.findDefaultSubjects());
    //
    //        return Completable.merge(completableList);
    //    }
    //    @NotNull
    //    private Date getFirstDayOfLastWeek() {
    //        GregorianCalendar c = new GregorianCalendar(new Locale("ru"));
    //        c.add(Calendar.WEEK_OF_MONTH, -1);
    //        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    //        return DateFormatUtil.toUtc(DateFormatUtil.removeTime(c.getTime()));
    //    }
    fun tryAuthWithPhoneNumByCode(code: String?) {
        authRepository.authByPhoneNum(code)
    }

    override fun removeListeners() {
        groupInfoRepository.removeListeners()
        groupRepository.removeListeners()
        authRepository.removeListeners()
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
        userRepository.removeListeners()
    }

    fun resendCode() {
        authRepository.resendCodeSms()
    }
}