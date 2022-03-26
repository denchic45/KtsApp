package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.EventsOfDay
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
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.toDateUTC
import com.denchic45.kts.utils.toLocalDate
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import javax.inject.Inject

class EventRepository @Inject constructor(
    override val context: Context,
    override val networkService: NetworkService,
    private val coroutineScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    override val userMapper: UserMapper,
    override val groupMapper: GroupMapper,
    override val specialtyMapper: SpecialtyMapper,
    override val dataBase: DataBase,
    private val eventDao: EventDao,
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
    private val appPreference: AppPreference,
    override val appVersionService: AppVersionService
) : Repository(context), SaveGroupOperation {

    private val groupsRef = firestore.collection("Groups")
    private val daysRef: Query = firestore.collectionGroup("Days")

    fun findEventsOfDayByGroupIdAndDate(groupId: String, date: LocalDate): Flow<EventsOfDay> {
        addListenerRegistrationIfNotExist("$date of $groupId") {
            getQueryOfEventsOfGroupByDate(date, groupId)
                .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    coroutineScope.launch(dispatcher) {
                        snapshots?.let {
                            if (!snapshots.isEmpty) {
                                saveDay(snapshots.toObjects(DayDoc::class.java)[0])
                            }
                        }
                    }
                }
        }
        return eventDao.observeEventsByDateAndGroupId(date, groupId)
            .map { eventMapper.entitiesToEventsOfDay(it, date) }
    }

    fun findEventsOfDayByYourGroupAndDate(date: LocalDate): Flow<EventsOfDay> {
        return callbackFlow {
            groupPreference.observeGroupId
                .filterNotNull()
                .collect { groupId ->
                    Log.d("lol", "ON events flatMap: ")
                    launch {
                        eventDao.observeEventsByDateAndGroupId(date, groupId)
                            .distinctUntilChanged()
                            .map { eventMapper.entitiesToEventsOfDay(it, date) }
                            .collect {
                                Log.d("lol", "ON events collect dao: ")
                                send(it)
                            }
                    }
                    if (date.toDateUTC() > nextSaturday || date.toDateUTC() < previousMonday) {
                        Log.d("lol", "ON available date ")
                        Log.d("lol", "ON events coroutine launch: ")

                        val registration = getQueryOfEventsOfGroupByDate(
                            date,
                            groupId
                        ).addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                            Log.d("lol", "ON events snapshot: ")
                            if (error != null) {
                                Log.w("lol", "ON events error:", error)
                                return@addSnapshotListener
                            }
                            if (!snapshots!!.isEmpty) {
                                launch {
                                    saveDay(snapshots.toObjects(DayDoc::class.java)[0])
                                }
                            }
                        }
                        awaitClose {
                            Log.d("lol", "ON event awaitClose: ")
                            registration.remove()
                        }
                    }
                }
        }
    }

    fun findEventsForDayForTeacherByDate(date: LocalDate): Flow<EventsOfDay> {
        return callbackFlow {
            Log.d("lol", "ON callback flow: ")
            val teacherId = userPreference.id

            launch {
                Log.d("lol", "ON launch dao: ")
                eventDao.observeEventsByDateAndTeacherId(date, teacherId)
                    .onEach { Log.d("lol", "ON each: ") }
                    .distinctUntilChanged()
                    .map { eventMapper.entitiesToEventsOfDay(it, date) }
                    .collect {
                        Log.d("lol", "ON collect: ")
                        send(it)
                    }
            }


            val eventsOfTeacherByDate = eventsOfTeacherByDate(date, teacherId)
            awaitClose {
                Log.d("lol", "ON awaitClose: ")
                eventsOfTeacherByDate.remove()
            }


        }
    }

    private fun getQueryOfEventsOfGroupByDate(date: LocalDate, groupId: String): Query {
        val toDate = date.toDateUTC()
        return groupsRef
            .document(groupId)
            .collection("Days")
            .whereEqualTo("date", toDate)
    }

    private suspend fun saveDay(dayDoc: DayDoc) {
        dataBase.withTransaction {
            courseDao.getNotRelatedTeacherIdsToGroup(dayDoc.teacherIds, dayDoc.groupId)
                .map { teacherId ->
                    firestore.collection("Users").document(teacherId)
                        .get()
                        .await()
                        .apply {
                            userDao.upsert(
                                userMapper.docToEntity(toObject(UserDoc::class.java)!!)
                            )
                        }
                }

            courseDao.getNotRelatedSubjectIdsToGroup(dayDoc.subjectIds, dayDoc.groupId)
                .map { subjectId: String ->
                    firestore.collection("Subjects").document(subjectId)
                        .get()
                        .await()
                        .apply {
                            subjectDao.upsert(
                                subjectMapper.docToEntity(toObject(SubjectDoc::class.java)!!)
                            )
                        }
                }

            dayDao.upsert(DayEntity(dayDoc.id, dayDoc.date.toLocalDate(), dayDoc.groupId))
            val eventEntities = eventMapper.docToEntity(dayDoc.events)
            eventDao.replaceByDateAndGroup(eventEntities, dayDoc.date.toLocalDate(), dayDoc.groupId)
            val teacherEventCrossRefs =
                eventMapper.lessonEntitiesToTeacherLessonCrossRefEntities(eventEntities)

            teacherEventDao.upsert(teacherEventCrossRefs)
        }
    }

    private fun eventsOfTeacherByDate(
        date: LocalDate,
        teacherId: String
    ): ListenerRegistration {
        return firestore.collectionGroup("Days")
            .whereArrayContains("teacherIds", teacherId)
            .whereEqualTo("date", date.toDateUTC())
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "getLessonsOfTeacherByDateListener: ", error)
                    return@addSnapshotListener
                }
                if (!snapshot!!.isEmpty) {
                    val dayDocs = snapshot.toObjects(DayDoc::class.java)
                    for (dayDoc in dayDocs) {
                        coroutineScope.launch(dispatcher) {
                            dataBase.withTransaction {
                                saveDay(dayDoc)
                                if (!groupDao.isExistSync(dayDoc.groupId)) {
                                    val documentSnapshot = groupsRef.document(dayDoc.groupId)
                                        .get()
                                        .await()
                                    if (documentSnapshot.exists())
                                        saveGroup(
                                            documentSnapshot.toObject(GroupDoc::class.java)!!
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

    fun observeEventsOfYourGroup() {
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
                                saveDay(dayDoc)
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
            .toDateUTC()

    private val previousMonday: Date
        get() = LocalDate.now()
            .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
            .minusWeeks(1)
            .toDateUTC()

    suspend fun addGroupTimetables(groupTimetables: List<GroupTimetable>) {
        if (isNetworkNotAvailable) throw NetworkException()
        val batch = firestore.batch()
        groupTimetables.map { groupTimetable: GroupTimetable ->
            addGroupTimetable(batch, groupTimetable)
        }
        batch.commit().await()
    }

    private suspend fun addGroupTimetable(
        batch: WriteBatch,
        groupTimetable: GroupTimetable
    ) {
        val groupWeekEvents = groupTimetable.weekEvents
        val dayRef = groupsRef.document(groupTimetable.group.id).collection("Days")
        eventDao.deleteByGroupAndDateRange(
            groupTimetable.group.id,
            groupWeekEvents[0].date,
            groupWeekEvents[5].date
        )

        val existsDayDocs: List<DayDoc> = getQueryOfWeekDays(groupTimetable, dayRef)
            .await()
            .toObjects(DayDoc::class.java)

        for (eventsOfTheDay in groupWeekEvents) {
            val maybeDayDoc = findDayByDate(existsDayDocs, eventsOfTheDay.date.toDateUTC())

            val addableEvents = eventMapper.domainToDoc(eventsOfTheDay.events)

            val dayDoc: DayDoc = maybeDayDoc?.let {
                it.events = addableEvents
                it
            } ?: DayDoc(
                date = eventsOfTheDay.date.toDateUTC(),
                _events = addableEvents,
                groupId = groupTimetable.group.id
            )

            batch[dayRef.document(dayDoc.id), dayDoc] = SetOptions.merge()

            dayDao.upsert(
                DayEntity(
                    dayDoc.id,
                    dayDoc.date.toLocalDate(),
                    groupTimetable.group.id
                )
            )
        }

    }

    private fun findDayByDate(dayDocs: List<DayDoc>, date: Date): DayDoc? {
        return dayDocs.firstOrNull { dayDoc: DayDoc -> dayDoc.date == date }
    }

    private fun getQueryOfWeekDays(
        groupTimetable: GroupTimetable,
        daysRef: CollectionReference
    ): Task<QuerySnapshot> {
        val monday = groupTimetable.weekEvents[0].date.toDateUTC()
        val saturday = groupTimetable.weekEvents[5].date.toDateUTC()
        return daysRef.whereGreaterThanOrEqualTo("date", monday)
            .whereLessThanOrEqualTo("date", saturday)
            .get()
    }


    suspend fun updateEventsOfDay(events: List<Event>, date: LocalDate, group: CourseGroup) {
        val dayDocId = dayDao.getIdByDateAndGroupId(date, group.id)
        if (isNetworkNotAvailable) return
        val daysRef = groupsRef.document(group.id)
            .collection("Days")
        if (dayDocId != null) {
            val snapshot = daysRef.whereEqualTo("date", date.toDateUTC())
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val dayDoc = snapshot.documents[0].toObject(DayDoc::class.java)
                dayDao.insert(DayEntity(dayDoc!!.id, dayDoc.date.toLocalDate(), group.id))

                val eventDocs = eventMapper.domainToDoc(events)
                daysRef.document(dayDoc.id)
                    .update(
                        "events",
                        eventDocs,
                        "timestamp",
                        FieldValue.serverTimestamp()
                    ).await()
            }
        } else {
            val dayDoc = DayDoc(
                date = date.toDateUTC(),
                _events = eventMapper.domainToDoc(events),
                groupId = group.id,
            )
            daysRef.document(dayDoc.id).set(dayDoc, SetOptions.merge()).await()
            dayDao.upsert(DayEntity(dayDoc.id, dayDoc.date.toLocalDate(), group.id))
        }
    }

}