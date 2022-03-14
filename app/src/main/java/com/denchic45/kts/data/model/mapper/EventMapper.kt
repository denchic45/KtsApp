package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.CourseGroup;
import com.denchic45.kts.data.model.domain.EmptyEventDetails;
import com.denchic45.kts.data.model.domain.Event;
import com.denchic45.kts.data.model.domain.EventDetails;
import com.denchic45.kts.data.model.domain.EventsOfDay;
import com.denchic45.kts.data.model.domain.Lesson;
import com.denchic45.kts.data.model.domain.SimpleEventDetails;
import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.firestore.EventDetailsDoc;
import com.denchic45.kts.data.model.firestore.EventDoc;
import com.denchic45.kts.data.model.room.EventEntity;
import com.denchic45.kts.data.model.room.EventWithSubjectAndTeachersEntities;
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity;
import com.denchic45.kts.data.model.room.TeacherEventCrossRef;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(uses = {CourseContentMapper.class, UserMapper.class, GroupMapper.class, SubjectMapper.class})
public interface EventMapper {

    @Mapping(source = "entities.groupEntity", target = ".")
    CourseGroup groupWithCuratorAndSpecialtyEntityToCourseGroup(GroupWithCuratorAndSpecialtyEntity entities);

    @Named("mapDetails")
    default EventDetails mapDetails(@NonNull EventWithSubjectAndTeachersEntities entities) {
        EventEntity.TYPE type = entities.getEventEntity().getType();
        if (type.equals(EventEntity.TYPE.LESSON)) {
            return eventEntityToLesson(entities);
        } else if (type.equals(EventEntity.TYPE.SIMPLE)) {
            return eventEntityToSimple(entities);
        } else if (type.equals(EventEntity.TYPE.EMPTY)) {
            return eventEntityToEmpty(entities);
        } else
            throw new IllegalArgumentException();
    }

    @Mapping(source = "teacherEntities", target = "teachers")
    @Mapping(source = "subjectEntity", target = "subject")
    Lesson eventEntityToLesson(EventWithSubjectAndTeachersEntities entities);

    @Mapping(source = "eventEntity", target = ".")
    SimpleEventDetails eventEntityToSimple(EventWithSubjectAndTeachersEntities entities);

    EmptyEventDetails eventEntityToEmpty(EventWithSubjectAndTeachersEntities entities);

    default List<Event> entityToDomain(@NonNull List<EventWithSubjectAndTeachersEntities> eventWithSubjectAndTeachersEntities) {
        return eventWithSubjectAndTeachersEntities.stream()
                .map(this::eventEntityToEvent)
                .collect(Collectors.toList());
    }

    default EventsOfDay entitiesToEventsOfDay(@NonNull List<EventWithSubjectAndTeachersEntities> eventWithSubjectAndTeachersEntities, LocalDate date) {
        return new EventsOfDay(date, entityToDomain(eventWithSubjectAndTeachersEntities));
    }

    @Mapping(source = "groupEntity", target = "group")
    @Mapping(source = "eventEntity", target = ".")
    @Mapping(target = "details", qualifiedByName = "mapDetails", source = ".")
    Event eventEntityToEvent(EventWithSubjectAndTeachersEntities eventEntity);

    @Named("mapTeacherList")
    default List<String> mapTeacherList(@NotNull List<User> teachers) {
        return teachers.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    List<EventEntity> docToEntity(List<EventDoc> doc);

    default EventDoc domainToDoc(Event domain) {
        EventDoc eventDoc = eventToEventDoc(domain);
        eventDoc.setEventDetailsDoc(detailsToDetailsDoc(domain.getDetails()));
        return eventDoc;
    }

    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "details", target = "eventDetailsDoc", qualifiedByName = "detailsToDetailsDoc")
    EventDoc eventToEventDoc(Event domain);

    default List<EventDoc> domainToDoc(@NonNull List<Event> docs) {
        return docs.stream()
                .map(this::domainToDoc)
                .collect(Collectors.toList());
    }

    @Named("detailsToDetailsDoc")
    default EventDetailsDoc detailsToDetailsDoc(@NonNull EventDetails eventDetails) {
        if (eventDetails.getType().equals(EventEntity.TYPE.LESSON))
            return lessonToDetailsDoc((Lesson) eventDetails);
        else if (eventDetails.getType().equals(EventEntity.TYPE.SIMPLE))
            return simpleToDetailsDoc((SimpleEventDetails) eventDetails);
        else if (eventDetails.getType().equals(EventEntity.TYPE.EMPTY))
            return emptyToDetailsDoc();
        throw new IllegalArgumentException();
    }

    @Mapping(source = "teachers", target = "teacherIds", qualifiedByName = "mapTeacherList")
    @Mapping(source = "subject.id", target = "subjectId")
    EventDetailsDoc lessonToDetailsDoc(Lesson eventDetails);

    EventDetailsDoc simpleToDetailsDoc(SimpleEventDetails eventDetails);

    default EventDetailsDoc emptyToDetailsDoc() {
        return EventDetailsDoc.Companion.createEmpty();
    }

    @Mapping(source = "eventDetailsDoc", target = ".")
    EventEntity docToEntity(EventDoc doc);

    EventDoc entityToDoc(EventEntity entity);

    default List<TeacherEventCrossRef> lessonEntitiesToTeacherLessonCrossRefEntities(@NotNull List<EventEntity> lessonEntities) {
        return lessonEntities.stream()
                .flatMap((Function<EventEntity, Stream<TeacherEventCrossRef>>) lessonEntity -> lessonEntity.getTeacherIds()
                        .stream()
                        .map(id -> new TeacherEventCrossRef(lessonEntity.getId(), id)))
                .collect(Collectors.toList());
    }
}
