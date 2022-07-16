package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.firestore.EventDetailsDoc
import com.denchic45.kts.data.model.firestore.EventDetailsDoc.Companion.createEmpty
import com.denchic45.kts.data.model.firestore.EventDoc
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import com.denchic45.kts.data.model.room.EventWithSubjectAndTeachersEntities
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import com.denchic45.kts.data.model.room.TeacherEventCrossRef
import com.denchic45.kts.domain.model.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.time.LocalDate

@Mapper(uses = [CourseContentMapper::class, UserMapper::class, GroupMapper::class, SubjectMapper::class])
abstract class EventMapper {
    @Mapping(source = "entities.groupEntity", target = ".")
    abstract fun groupWithCuratorAndSpecialtyEntityToCourseGroup(entities: GroupWithCuratorAndSpecialtyEntity): GroupHeader

    @Named("mapDetails")
    fun mapDetails(entities: EventWithSubjectAndTeachersEntities): EventDetails {
        return when (entities.eventEntity.type) {
            TYPE.LESSON -> eventEntityToLesson(entities)
            TYPE.SIMPLE -> eventEntityToSimple(entities)
            TYPE.EMPTY -> eventEntityToEmpty(entities)
        }
    }

    @Mapping(source = "teacherEntities", target = "teachers")
    @Mapping(source = "subjectEntity", target = "subject")
    abstract fun eventEntityToLesson(entities: EventWithSubjectAndTeachersEntities): Lesson

    @Mapping(source = "eventEntity", target = ".")
    abstract fun eventEntityToSimple(entities: EventWithSubjectAndTeachersEntities): SimpleEventDetails

    abstract fun eventEntityToEmpty(entities: EventWithSubjectAndTeachersEntities): EmptyEventDetails

    fun entityToDomain(eventWithSubjectAndTeachersEntities: List<EventWithSubjectAndTeachersEntities>): List<Event> {
        return eventWithSubjectAndTeachersEntities
            .map { eventEntity: EventWithSubjectAndTeachersEntities ->
                eventEntityToEvent(eventEntity)
            }
    }

    fun entitiesToEventsOfDay(
        eventWithSubjectAndTeachersEntities: List<EventWithSubjectAndTeachersEntities>,
        date: LocalDate
    ): EventsOfDay {
        return EventsOfDay(date, entityToDomain(eventWithSubjectAndTeachersEntities))
    }

    @Mapping(source = "groupEntity", target = "groupHeader")
    @Mapping(source = "eventEntity", target = ".")
    @Mapping(target = "details", qualifiedByName = ["mapDetails"], source = ".")
    abstract fun eventEntityToEvent(eventEntity: EventWithSubjectAndTeachersEntities): Event

    @Named("mapTeacherList")
    fun mapTeacherList(teachers: List<User>): List<String> {
        return teachers.map(User::id)
    }

    fun docToEntity(docs: List<EventDoc>, dayId: String): List<EventEntity> {
        return docs.map { eventDoc ->
            docToEntity(eventDoc, dayId)
        }
    }

    fun domainToDoc(domain: Event, index: Int): EventDoc {
        val eventDoc = eventToEventDoc(domain, index)
        eventDoc.eventDetailsDoc = detailsToDetailsDoc(domain.details)
        return eventDoc
    }

    @Mapping(source = "domain.groupHeader.id", target = "groupId")
    @Mapping(
        source = "domain.details",
        target = "eventDetailsDoc",
        qualifiedByName = ["detailsToDetailsDoc"]
    )
    abstract fun eventToEventDoc(domain: Event, position: Int): EventDoc

    fun domainToDoc(docs: List<Event>): List<EventDoc> {
        return docs.withIndex()
            .map { (index, event) -> this.domainToDoc(event, index) }
    }

    @Named("detailsToDetailsDoc")
    fun detailsToDetailsDoc(eventDetails: EventDetails): EventDetailsDoc {
        return when (eventDetails.type) {
            TYPE.LESSON -> lessonToDetailsDoc(eventDetails as Lesson)
            TYPE.SIMPLE -> simpleToDetailsDoc(eventDetails as SimpleEventDetails)
            TYPE.EMPTY -> emptyToDetailsDoc()
        }
    }

    @Mapping(source = "teachers", target = "teacherIds", qualifiedByName = ["mapTeacherList"])
    @Mapping(source = "subject.id", target = "subjectId")
    abstract fun lessonToDetailsDoc(eventDetails: Lesson): EventDetailsDoc

    abstract fun simpleToDetailsDoc(
        simpleEventDetails: SimpleEventDetails
    ): EventDetailsDoc

    private fun emptyToDetailsDoc(): EventDetailsDoc {
        return createEmpty()
    }

    @Mapping(source = "dayId", target = "dayId")
    @Mapping(source = "doc.eventDetailsDoc", target = ".")
    abstract fun docToEntity(doc: EventDoc, dayId: String): EventEntity

    abstract fun entityToDoc(entity: EventEntity): EventDoc

    fun lessonEntitiesToTeacherLessonCrossRefEntities(eventEntities: List<EventEntity>): List<TeacherEventCrossRef> {
        return eventEntities
            .filter { !it.teacherIds.isNullOrEmpty() }
            .flatMap { eventEntity ->
                eventEntity.teacherIds!!.map { id: String ->
                    TeacherEventCrossRef(
                        eventEntity.id,
                        id
                    )
                }
            }
    }
}