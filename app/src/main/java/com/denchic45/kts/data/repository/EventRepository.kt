package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.GroupTimetable
import com.denchic45.kts.data.model.firestore.DayDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectDoc
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.DayEntity
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.toDate
import com.denchic45.kts.utils.toLocalDate
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import javax.inject.Inject

class EventRepository @Inject constructor(
    context: Context,
    override val networkService: NetworkService,
    private val coroutineScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    override val userMapper: UserMapper,
    override val groupMapper: GroupMapper,
    override val specialtyMapper: SpecialtyMapper,
    override val dataBase: DataBase,
    private val lessonDao: LessonDao,
    override val userDao: UserDao,
    private val courseDao: CourseDao,
    private val teacherEventDao: TeacherEventDao,
    private val courseContentDao: CourseContentDao,
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

    fun findLessonsOfGroupByDate(date: LocalDate, groupId: String): Flow<List<Event>> {
        addListenerRegistrationIfNotExist("$date of $groupId") {
            getQueryOfLessonsOfGroupByDate(
                date,
                groupId
            ).addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.w("lol", "listen:error", error)
                    return@addSnapshotListener
                }
                coroutineScope.launch(dispatcher) {
                    if (!snapshots!!.isEmpty) {
                        saveDay2(snapshots.toObjects(DayDoc::class.java)[0])
                    }
                }
            }
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndGroupId(date, groupId)
            .map { eventMapper.entityToDomain(it) }
    }

    fun findLessonOfYourGroupByDate(date: LocalDate, groupId: String): Flow<List<Event>> {
        if (date.toDate() > nextSaturday || date.toDate() < previousMonday) {
            addListenerRegistrationIfNotExist("$date of $groupId") {
                getQueryOfLessonsOfGroupByDate(
                    date,
                    groupId
                ).addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.w("lol", "listen:error", error)
                        return@addSnapshotListener
                    }
                    if (!snapshots!!.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            saveDay2(snapshots.toObjects(DayDoc::class.java)[0])
                        }
                    }
                }
            }
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndGroupId(date, groupId)
            .distinctUntilChanged()
            .map { eventMapper.entityToDomain(it) }
    }

    fun findLessonsForTeacherByDate(date: LocalDate): Flow<List<Event>> {
        val teacherId = userPreference.id
        addListenerRegistrationIfNotExist("$date of teacher") {
            getListenerOfLessonsOfTeacherByDate(date, teacherId)
        }

        return lessonDao.getLessonWithHomeWorkWithSubjectByDateAndTeacherId(date, teacherId)
            .distinctUntilChanged()
            .map { eventMapper.entityToDomain(it) }
    }

    private fun getQueryOfLessonsOfGroupByDate(date: LocalDate, groupId: String): Query {
        return groupsRef
            .document(groupId)
            .collection("Days")
            .whereEqualTo("date", date.toDate())
    }

//    private fun saveDay(dayDoc: DayDoc) {
//        val completableList: MutableList<Completable> = ArrayList()
//        completableList.addAll(
//            courseDao.getNotRelatedTeacherIdsToGroup(dayDoc.teacherIds, dayDoc.groupId).stream()
//                .map { teacherId: String ->
//                    Completable.create { emitter: CompletableEmitter ->
//                        firestore.collection("Users").document(teacherId)
//                            .get()
//                            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
//                                coroutineScope.launch(dispatcher) {
//                                    userDao.upsert(
//                                        userMapper.docToEntity(
//                                            documentSnapshot.toObject(UserDoc::class.java)!!
//                                        )
//                                    )
//                                    emitter.onComplete()
//                                }
//                            }.addOnFailureListener { t: Exception -> emitter.onError(t) }
//                    }
//                }
//                .collect(Collectors.toList()))
//        completableList.addAll(
//            courseDao.getNotRelatedSubjectIdsToGroup(dayDoc.subjectIds, dayDoc.groupId).stream()
//                .map { subjectId: String ->
//                    Completable.create { emitter: CompletableEmitter ->
//                        firestore.collection("Subjects").document(subjectId)
//                            .get()
//                            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
//                                coroutineScope.launch(dispatcher) {
//                                    subjectDao.upsert(
//                                        subjectMapper.docToEntity(
//                                            documentSnapshot.toObject(
//                                                SubjectDoc::class.java
//                                            )
//                                        )
//                                    )
//                                    emitter.onComplete()
//                                }
//                            }.addOnFailureListener(emitter::onError)
//                    }
//                }
//                .collect(Collectors.toList())
//        )
//        Completable.concat(completableList)
//            .observeOn(Schedulers.io())
//            .subscribeOn(Schedulers.io())
//            .subscribe {
//                dataBase.runInTransaction {
//                    coroutineScope.launch(dispatcher) {
//                        dayDao.upsert(DayEntity(dayDoc.id, dayDoc.date, dayDoc.groupId))
//                        val eventEntities = eventMapper.docToEntity(dayDoc.events)
//                        lessonDao.replaceByDateAndGroup(
//                            eventEntities,
//                            dayDoc.date,
//                            dayDoc.groupId
//                        )
//                        val teacherEventCrossRefs =
//                            eventMapper.lessonEntitiesToTeacherLessonCrossRefEntities(eventEntities)
//                        teacherEventDao.upsert(teacherEventCrossRefs)
//                        courseContentDao.upsert(dayDoc.homework)
//                    }
//                }
//            }
//    }

    private suspend fun saveDay2(dayDoc: DayDoc) {
        courseDao.getNotRelatedTeacherIdsToGroup(dayDoc.teacherIds, dayDoc.groupId)
            .map { teacherId: String ->
                val documentSnapshot = firestore.collection("Users").document(teacherId)
                    .get()
                    .await()

                coroutineScope.launch(dispatcher) {
                    userDao.upsert(
                        userMapper.docToEntity(documentSnapshot.toObject(UserDoc::class.java)!!)
                    )
                }
            }

        courseDao.getNotRelatedSubjectIdsToGroup(dayDoc.subjectIds, dayDoc.groupId)
            .map { subjectId: String ->
                val documentSnapshot = firestore.collection("Subjects").document(subjectId)
                    .get()
                    .await()
                coroutineScope.launch(dispatcher) {
                    subjectDao.upsert(
                        subjectMapper.docToEntity(documentSnapshot.toObject(SubjectDoc::class.java))
                    )
                }
            }

        dataBase.withTransaction {
            dayDao.upsert(DayEntity(dayDoc.id, dayDoc.date, dayDoc.groupId))
            val eventEntities = eventMapper.docToEntity(dayDoc.events)
            lessonDao.replaceByDateAndGroup(eventEntities, dayDoc.date.toLocalDate(), dayDoc.groupId)
            val teacherEventCrossRefs =
                eventMapper.lessonEntitiesToTeacherLessonCrossRefEntities(eventEntities)
            teacherEventDao.upsert(teacherEventCrossRefs)
            courseContentDao.upsert(dayDoc.homework)
        }
    }

    private fun getListenerOfLessonsOfTeacherByDate(
        date: LocalDate,
        teacherId: String
    ): ListenerRegistration {
        return firestore.collectionGroup("Days")
            .whereArrayContains("teacherIds", teacherId)
            .whereEqualTo("date", DateFormatUtil.convertDateToDateUTC(date.toDate()))
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "getLessonsOfTeacherByDateListener: ", error)
                    return@addSnapshotListener
                }
                if (!snapshot!!.isEmpty) {
                    val dayDocs = snapshot.toObjects(DayDoc::class.java)
                    for (dayDoc in dayDocs) {
                        coroutineScope.launch(dispatcher) {
                            if (groupDao.isExistSync(dayDoc.groupId)) {
                                saveDay2(dayDoc)
                            } else {
                                val documentSnapshot = groupsRef.document(dayDoc.groupId)
                                    .get()
                                    .await()
                                dataBase.withTransaction {
                                    if (documentSnapshot.exists())
                                        saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                                            documentSnapshot.toObject(GroupDoc::class.java)!!
                                        )
                                    saveDay2(dayDoc)
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

    fun listenLessonsOfYourGroup() {
        if (!hasListener("lessonsOfYouGroup")) addListenerRegistrationIfNotExist("lessonsOfYouGroup") {
            daysRef.whereGreaterThanOrEqualTo("date", previousMonday)
                .whereLessThanOrEqualTo("date", nextSaturday)
                .whereEqualTo("groupId", groupPreference.groupId)
                .addSnapshotListener { querySnapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.d("lol", "onEvent: ", error)
                        return@addSnapshotListener
                    }
                    if (!querySnapshot!!.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            for (dayDoc in querySnapshot.toObjects(DayDoc::class.java)) {
                                saveDay2(dayDoc)
                            }
                        }
                    }
                }
        }
    }

    private val nextSaturday: Date
        get() = LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            .plusWeeks(1)
            .toDate()

    private val previousMonday: Date
        get() = LocalDate.now()
            .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
            .minusWeeks(1)
            .toDate()


    fun loadLessonsInDateRange(startDate: Date): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            daysRef
                .whereGreaterThanOrEqualTo("date", startDate!!)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    if (!snapshots.isEmpty) {
                        coroutineScope.launch(dispatcher) {
                            for (dayDoc in snapshots.toObjects(DayDoc::class.java)) {
                                lessonDao.insert(eventMapper.docToEntity(dayDoc.events))
                                courseContentDao.insert(dayDoc.homework)
                            }
                        }
                    }
                    emitter.onComplete()
                }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    suspend fun addLessonsOfWeekForGroups(groupTimetableList: List<GroupTimetable>) {
        if (isNetworkNotAvailable) throw NetworkException()
        val batch = firestore.batch()
        groupTimetableList
            .map { groupTimetable: GroupTimetable ->
                addLessonsOfWeekForGroup(batch, groupTimetable)
            }
        batch.commit().await()
    }

    private suspend fun addLessonsOfWeekForGroup(
        batch: WriteBatch,
        groupTimetable: GroupTimetable
    ) {
        val weekLessons = groupTimetable.weekEvents
        val dayRef = groupsRef.document(groupTimetable.group.id).collection("Days")
        coroutineScope.launch(dispatcher) {
            lessonDao.deleteByGroupAndDateRange(
                groupTimetable.group.id,
                weekLessons[0].date,
                weekLessons[5].date
            )
        }
        val dayDocs: List<DayDoc> = getQueryOfWeekDays(groupTimetable, dayRef)
            .await()
            .toObjects(DayDoc::class.java)

        for (eventsOfTheDay in weekLessons) {
            val optionalDayDoc = findByDate(dayDocs, eventsOfTheDay.date.toDate())
            // todo ВАЖНО СРАВНИТЬ ДАТУ!!!
            val addableEvents = eventMapper.domainToDoc(eventsOfTheDay.events)
            val dayDoc: DayDoc
            if (optionalDayDoc.isPresent) {
                dayDoc = optionalDayDoc.get()
                dayDoc.events = addableEvents
                dayDoc.timestamp = null
            } else {
                dayDoc = DayDoc(
                    date = eventsOfTheDay.date.toDate(),
                    _events = addableEvents,
                    groupId = groupTimetable.group.id
                )
            }
            batch[dayRef.document(dayDoc.id), dayDoc] = SetOptions.merge()
            coroutineScope.launch(dispatcher) {
                dayDao.upsert(
                    DayEntity(
                        dayDoc.id,
                        dayDoc.date,
                        groupTimetable.group.id
                    )
                )
            }
        }
    }

    private fun findByDate(dayDocs: List<DayDoc>, date: Date): Optional<DayDoc> {
        return dayDocs.stream()
            .filter { dayDoc: DayDoc -> dayDoc.date == date }
            .findFirst()
    }

    private fun getQueryOfWeekDays(
        groupTimetable: GroupTimetable,
        daysRef: CollectionReference
    ): Task<QuerySnapshot> {
        val monday = DateFormatUtil.convertDateToDateUTC(groupTimetable.weekEvents[0].date.toDate())
        val saturday =
            DateFormatUtil.convertDateToDateUTC(groupTimetable.weekEvents[5].date.toDate())
        return daysRef.whereGreaterThanOrEqualTo("date", monday)
            .whereLessThanOrEqualTo("date", saturday)
            .get()
    }


    suspend fun updateEventsOfDay(events: List<Event>, date: LocalDate, group: CourseGroup) {
        val dayDocId = dayDao.getIdByDateAndGroupId(date.toDate(), group.id)
        if (isNetworkNotAvailable) return
        val daysRef = groupsRef.document(group.id)
            .collection("Days")
        if (dayDocId != null) {
            val toDate = date.toDate()
            val snapshot = daysRef.whereEqualTo("date", toDate)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val dayDoc = snapshot.documents[0].toObject(DayDoc::class.java)
                dayDao.insert(DayEntity(dayDoc!!.id, dayDoc.date, group.id))
                // todo ВАЖНО СРАВНИТЬ ДАТУ!!!
                val value = eventMapper.domainToDoc(events)
                daysRef.document(dayDoc.id)
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
                date = date.toDate(),
                _events = eventMapper.domainToDoc(events),
                groupId = group.id,
            )
            daysRef.document(dayDoc.id).set(dayDoc, SetOptions.merge()).await()
            coroutineScope.launch(dispatcher) {
                dayDao.upsert(DayEntity(dayDoc.id, dayDoc.date, group.id))
            }
        }
    }

}