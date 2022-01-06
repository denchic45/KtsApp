package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.EventsOfTheDay
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.GroupWeekLessons
import com.denchic45.kts.data.model.firestore.DayDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectDoc
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.*
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.kts.utils.NetworkException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.inject.Inject

class EventRepository @Inject constructor(
    context: Context,
    override val networkService: NetworkService,
    private val coroutineScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    override val userMapper: UserMapper,
    override val groupMapper: GroupMapper,
    override val specialtyMapper: SpecialtyMapper,
    private val dataBase: DataBase,
    private val lessonDao: LessonDao,
    override val userDao: UserDao,
    private val courseDao: CourseDao,
    private val teacherEventDao: TeacherEventDao,
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao,
    private val groupPreference: GroupPreference,
    private val firestore: FirebaseFirestore,
    private val eventMapper: EventMapper,
    private val subjectMapper: SubjectMapper,
    private val dayDao: DayDao,
    override val groupDao: GroupDao,
    override val specialtyDao: SpecialtyDao,
    private val userPreference: UserPreference,
    private val appPreference: AppPreference
) : Repository(context), IGroupRepository {
    private val groupsRef = firestore.collection("Groups")
    private val daysRef: Query = firestore.collectionGroup("Days")
    fun findLessonsOfGroupByDate(date: Date, groupUuid: String): Flow<List<Event>> {
        addListenerRegistrationIfNotExist("$date of $groupUuid") {
            getQueryOfLessonsOfGroupByDate(
                date,
                groupUuid
            ).addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.w("lol", "listen:error", error)
                    return@addSnapshotListener
                }
                coroutineScope.launch(dispatcher) {
                    dataBase.runInTransaction {
                        if (!snapshots!!.isEmpty) {
                            saveDay(snapshots.toObjects(DayDoc::class.java)[0])
                        }
                    }
                }
            }
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndGroupUuid(date, groupUuid)
            .map { eventMapper.entityToDomain(it) }
    }

    fun findLessonOfYourGroupByDate(date: Date, groupUuid: String): Flow<List<Event>> {
        if (date.after(nextSaturday) || date.before(previousMonday)) {
            addListenerRegistrationIfNotExist("$date of $groupUuid") {
                getQueryOfLessonsOfGroupByDate(
                    date,
                    groupUuid
                ).addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.w("lol", "listen:error", error)
                        return@addSnapshotListener
                    }
                    if (!snapshots!!.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            saveDay(snapshots.toObjects(DayDoc::class.java)[0])
                        }
                    }
                }
            }
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndGroupUuid(
            date,
            groupUuid
        )
            .distinctUntilChanged()
            .map {
                eventMapper.entityToDomain(it)
            }
    }

    fun findLessonsForTeacherByDate(date: Date): Flow<List<Event>> {
        val teacherUuid = userPreference.id
        addListenerRegistrationIfNotExist("$date of teacher") {
            getListenerOfLessonsOfTeacherByDate(
                date,
                teacherUuid
            )
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndTeacherUuid(
            date,
            teacherUuid
        )
            .distinctUntilChanged()
            .map {
                eventMapper.entityToDomain(
                    it
                )
            }
    }

    private fun getQueryOfLessonsOfGroupByDate(date: Date, groupUuid: String): Query {
        return groupsRef
            .document(groupUuid)
            .collection("Days")
            .whereEqualTo("date", DateFormatUtil.convertDateToDateUTC(date))
    }

    private fun saveDay(dayDoc: DayDoc) {
        val completableList: MutableList<Completable> = ArrayList()
        completableList.addAll(
            courseDao.getNotRelatedTeacherIdsToGroup(dayDoc.teacherIds, dayDoc.groupUuid).stream()
                .map { teacherUuid: String? ->
                    Completable.create { emitter: CompletableEmitter ->
                        firestore.collection("Users").document(
                            teacherUuid!!
                        )
                            .get()
                            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                                coroutineScope.launch(dispatcher) {
                                    userDao.upsert(
                                        userMapper.docToEntity(
                                            documentSnapshot.toObject(
                                                UserDoc::class.java
                                            )
                                        )
                                    )
                                    emitter.onComplete()
                                }
                            }.addOnFailureListener { t: Exception -> emitter.onError(t) }
                    }
                }
                .collect(Collectors.toList()))
        completableList.addAll(
            courseDao.getNotRelatedSubjectIdsToGroup(dayDoc.subjectIds, dayDoc.groupUuid).stream()
                .map { subjectUuid: String? ->
                    Completable.create { emitter: CompletableEmitter ->
                        firestore.collection("Subjects").document(subjectUuid!!)
                            .get()
                            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                                coroutineScope.launch(dispatcher) {
                                    subjectDao.upsert(
                                        subjectMapper.docToEntity(
                                            documentSnapshot.toObject(
                                                SubjectDoc::class.java
                                            )
                                        )
                                    )
                                    emitter.onComplete()
                                }
                            }.addOnFailureListener(emitter::onError)
                    }
                }
                .collect(Collectors.toList())
        )
        Completable.concat(completableList)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                dataBase.runInTransaction {
                    coroutineScope.launch(dispatcher) {
//                        lessonDao.deleteByDateAndGroup(dayDoc.date, dayDoc.groupUuid)
                        dayDao.upsert(DayEntity(dayDoc.uuid, dayDoc.date, dayDoc.groupUuid))
                        val eventEntities = eventMapper.docToEntity(dayDoc.events)
                        lessonDao.replaceByDateAndGroup(
                            eventEntities,
                            dayDoc.date,
                            dayDoc.groupUuid
                        )
                        val teacherEventCrossRefs =
                            eventMapper.lessonEntitiesToTeacherLessonCrossRefEntities(eventEntities)
                        teacherEventDao.upsert(teacherEventCrossRefs)

//                    List<Subject> subjects = dayDoc.getLessons().stream()
//                            .filter(eventDoc -> eventDoc.getSubject() != null)
//                            .map(EventDoc::getSubject)
//                            .distinct()
//                            .collect(Collectors.toList());
//                    subjectDao.upsert(subjectMapper.domainToEntity(subjects));
                        taskDao.upsert(dayDoc.homework)
                    }
                }
            }
    }

    private fun getListenerOfLessonsOfTeacherByDate(
        date: Date,
        teacherUuid: String
    ): ListenerRegistration {
        return firestore.collectionGroup("Days")
            .whereArrayContains("teacherIds", teacherUuid)
            .whereEqualTo("date", DateFormatUtil.convertDateToDateUTC(date))
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "getLessonsOfTeacherByDateListener: ", error)
                    return@addSnapshotListener
                }
                if (!snapshot!!.isEmpty) {
                    val dayDocs = snapshot.toObjects(DayDoc::class.java)
                    for (dayDoc in dayDocs) {
                        coroutineScope.launch(dispatcher) {
                            if (groupDao.isExistSync(dayDoc.groupUuid)) {
                                saveDay(dayDoc)
                            } else {
                                groupsRef.document(dayDoc.groupUuid)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                                        coroutineScope.launch(dispatcher) {
                                            dataBase.runInTransaction {
                                                launch {
                                                    if (documentSnapshot.exists())
                                                        saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                                                            documentSnapshot.toObject(GroupDoc::class.java)!!
                                                        )
                                                    saveDay(dayDoc)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Log.d(
                                            "lol",
                                            "getListenerOfLessonsOfTeacherByDate: ",
                                            e
                                        )
                                    }
                            }
                        }
                    }
                }
            }
    }

    var lessonTime: Int
        get() = appPreference.lessonTime
        set(lessonTime) {
            appPreference.lessonTime = lessonTime
        }

    fun updateHomeworkCompletion(checked: Boolean) {
//        homeworkDao.updateHomeworkCompletion(checked ? 1 : 0);
    }

    fun loadLessonsByTeacherUuidAndStartDate(teacherUuid: String?, startDate: Date?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            firestore.collectionGroup("Lessons")
                .whereArrayContains("teachersUuid", teacherUuid!!)
                .whereGreaterThanOrEqualTo("date", startDate!!)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        coroutineScope.launch(dispatcher) {
                            val eventEntityWithRequiredTeacherUser: MutableList<EventEntity> =
                                ArrayList()
                            for (dayDoc in task.result!!.toObjects(DayDoc::class.java)) {
                                eventEntityWithRequiredTeacherUser.addAll(eventMapper.docToEntity(
                                    dayDoc.events
                                )
                                    .stream()
                                    .filter { (_, _, _, _, _, _, teacherUuidList) ->
                                        teacherUuidList!!.contains(teacherUuid)
                                    }
                                    .collect(Collectors.toList()))
                            }
                            lessonDao.upsert(eventEntityWithRequiredTeacherUser)
                            for ((uuid, _, _, _, _, _, teacherUuidList) in eventEntityWithRequiredTeacherUser) {
                                teacherUuidList!!.forEach(Consumer { uuidOfTeacher: String? ->
                                    coroutineScope.launch(dispatcher) {
                                        teacherEventDao.upsert(
                                            TeacherEventCrossRef(
                                                uuid, uuidOfTeacher!!
                                            )
                                        )
                                    }
//                                      coroutineScope.launch(dispatcher) {
                                })
                            }
                            emitter.onComplete()
                        }
                    } else {
                        emitter.onError(task.exception)
                    }
                }
        }
    }

    fun listenLessonsOfYourGroup() {
        if (!hasListener("lessonsOfYouGroup")) addListenerRegistrationIfNotExist("lessonsOfYouGroup") {
            daysRef.whereGreaterThanOrEqualTo("date", previousMonday)
                .whereLessThanOrEqualTo("date", nextSaturday)
                .whereEqualTo(
                    "groupUuid",
                    groupPreference.groupUuid
                ) //                    .whereGreaterThanOrEqualTo("timestamp", timestampPreference.getLastUpdateEventsTimestamp())
                .addSnapshotListener { querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.d("lol", "onEvent: ", error)
                        return@addSnapshotListener
                    }
                    if (!querySnapshot!!.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            for (dayDoc in querySnapshot.toObjects(DayDoc::class.java)) {
                                saveDay(dayDoc)
                            }
                        }
                    }
                }
        }
    }

    private val nextSaturday: Date
        get() = Date.from(
            LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                .plusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
    private val previousMonday: Date
        get() = Date.from(
            LocalDate.now()
                .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                .minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

    fun loadLessonsInDateRange(startDate: Date?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            daysRef
                .whereGreaterThanOrEqualTo("date", startDate!!)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    if (!snapshots.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            for (dayDoc in snapshots.toObjects(DayDoc::class.java)) {
                                lessonDao.insert(eventMapper.docToEntity(dayDoc.events))
                                taskDao.insert(dayDoc.homework)
                            }
                        }
                    }
                    emitter.onComplete()
                }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
        }
    }

    fun addLessonsOfWeekForGroups(groupWeekLessonsList: List<GroupWeekLessons>): Completable {
        if (isNetworkNotAvailable) return Completable.error(NetworkException())
        val batch = firestore.batch()
        return Completable.concat(groupWeekLessonsList.stream()
            .map { groupWeekLessons: GroupWeekLessons ->
                addLessonsOfWeekForGroup(
                    batch,
                    groupWeekLessons
                )
            }
            .collect(Collectors.toList()))
            .doOnComplete { batch.commit() }
    }

    private fun addLessonsOfWeekForGroup(
        batch: WriteBatch,
        groupWeekLessons: GroupWeekLessons
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val weekLessons = groupWeekLessons.weekLessons
            val dayRef = groupsRef.document(groupWeekLessons.group.uuid).collection("Days")
            coroutineScope.launch(dispatcher) {
                lessonDao.deleteByGroupAndDateRange(
                    groupWeekLessons.group.uuid,
                    weekLessons[0].date,
                    weekLessons[5].date
                )
            }
            getQueryOfWeekDays(groupWeekLessons, dayRef)
                .addOnSuccessListener(object : OnSuccessListener<QuerySnapshot> {
                    private var dayDocs: List<DayDoc>? = null
                    override fun onSuccess(querySnapshot: QuerySnapshot) {
                        dayDocs = querySnapshot.toObjects(DayDoc::class.java)
                        for (eventsOfTheDay in weekLessons) {
                            setLessonsOfDays(eventsOfTheDay)
                        }
                        emitter.onComplete()
                    }

                    private fun setLessonsOfDays(eventsOfTheDay: EventsOfTheDay) {
                        val optionalDayDoc = findByDate(dayDocs!!, eventsOfTheDay.date)
                        // todo ВАЖНО СРАВНИТЬ ДАТУ!!!
                        val addableEvents = eventMapper.domainToDoc(eventsOfTheDay.events)
                        val dayDoc: DayDoc
                        if (optionalDayDoc.isPresent) {
                            dayDoc = optionalDayDoc.get()
                            dayDoc.events = addableEvents
                            dayDoc.timestamp = null
                            //                                    batch.update(dayRef.document(dayDoc.getUuid()), "lessons", addableLessons,  ,"timestamp", FieldValue.serverTimestamp());
                        } else {
                            dayDoc = DayDoc(
                                date = eventsOfTheDay.date,
                                _events = addableEvents,
                                groupUuid = groupWeekLessons.group.uuid
                            )
                        }
                        batch[dayRef.document(dayDoc.uuid), dayDoc] = SetOptions.merge()
                        coroutineScope.launch(dispatcher) {
                            dayDao.upsert(
                                DayEntity(
                                    dayDoc.uuid,
                                    dayDoc.date,
                                    groupWeekLessons.group.uuid
                                )
                            )
                        }
                    }

                    private fun findByDate(dayDocs: List<DayDoc>, date: Date): Optional<DayDoc> {
                        return dayDocs.stream()
                            .filter { dayDoc: DayDoc -> dayDoc.date == date }
                            .findFirst()
                    }
                })
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    private fun getQueryOfWeekDays(
        groupWeekLessons: GroupWeekLessons,
        daysRef: CollectionReference
    ): Task<QuerySnapshot> {
        val monday = DateFormatUtil.convertDateToDateUTC(groupWeekLessons.weekLessons[0].date)
        val saturday = DateFormatUtil.convertDateToDateUTC(groupWeekLessons.weekLessons[5].date)
        return daysRef.whereGreaterThanOrEqualTo("date", monday)
            .whereLessThanOrEqualTo("date", saturday)
            .get()
    }

    suspend fun updateEventsOfDay(events: List<Event>, date: Date, group: Group) {
        val dayDocUuid = dayDao.getUuidByDateAndGroupUuid(date, group.uuid)
        if (isNetworkNotAvailable) return
        val daysRef = groupsRef.document(group.uuid)
            .collection("Days")
        if (dayDocUuid != null) {
            val snapshot = daysRef.whereEqualTo("date", date)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val dayDoc = snapshot.documents[0].toObject(DayDoc::class.java)
                dayDao.insert(DayEntity(dayDoc!!.uuid, dayDoc.date, group.uuid))
                // todo ВАЖНО СРАВНИТЬ ДАТУ!!!
                val value = eventMapper.domainToDoc(events)
                daysRef.document(dayDoc.uuid)
                    .update(
                        "events",
                        value,
                        "timestamp",
                        FieldValue.serverTimestamp()
                    ).await()
            }
        } else {
            // todo ВАЖНО СРАВНИТЬ ДАТУ!!!
            val dayDoc = DayDoc(
                date = date,
                _events = eventMapper.domainToDoc(events),
                groupUuid = group.uuid
            )
            daysRef.document(dayDoc.uuid).set(dayDoc, SetOptions.merge()).await()
            coroutineScope.launch(dispatcher) {
                dayDao.upsert(DayEntity(dayDoc.uuid, dayDoc.date, group.uuid))
            }
        }
    }

}