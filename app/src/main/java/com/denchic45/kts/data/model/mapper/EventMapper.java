package com.denchic45.kts.data.model.mapper;

import androidx.annotation.NonNull;

import com.denchic45.kts.data.model.domain.EmptyEventDetails;
import com.denchic45.kts.data.model.domain.Event;
import com.denchic45.kts.data.model.domain.EventDetails;
import com.denchic45.kts.data.model.domain.Lesson;
import com.denchic45.kts.data.model.domain.SimpleEventDetails;
import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.firestore.EventDetailsDoc;
import com.denchic45.kts.data.model.firestore.EventDoc;
import com.denchic45.kts.data.model.room.EventEntity;
import com.denchic45.kts.data.model.room.EventTaskSubjectTeachersEntities;
import com.denchic45.kts.data.model.room.TeacherEventCrossRef;
import com.denchic45.kts.utils.DateFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(uses = {CourseContentMapper.class, UserMapper.class, GroupMapper.class, SubjectMapper.class})
public interface EventMapper extends
        DocEntityMapper<EventDoc, EventEntity> {
    default EventEntity domainToEntity(@NonNull Event domain) {
        EventDetails details = domain.getDetails();
        EventEntity eventEntity = eventToEventEntity(domain);
        mapDetails(eventEntity, domain);
        return eventEntity;
    }

    @AfterMapping
    default void mapDetails(@Context EventEntity eventEntity, @NonNull Event event) {
        if (event.getType().equals(EventEntity.TYPE.LESSON)) {
            Lesson lesson = (Lesson) event.getDetails();
            eventEntity.setSubjectId(lesson.getSubject().getId());
            eventEntity.setTeacherIds(mapTeacherList(lesson.getTeachers()));
        } else if (event.getType().equals(EventEntity.TYPE.SIMPLE)) {
            SimpleEventDetails simpleEventDetails = (SimpleEventDetails) event.getDetails();
            eventEntity.setName(simpleEventDetails.getName());
        } else if (event.getType().equals(EventEntity.TYPE.EMPTY)) {

        } else throw new IllegalArgumentException();
    }


    @Named("mapDetails")
    default EventDetails mapDetails(EventTaskSubjectTeachersEntities entities) {
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
    @Mapping(source = "courseContentEntity", target = "task")
    Lesson eventEntityToLesson(EventTaskSubjectTeachersEntities entities);

    @Mapping(source = "eventEntity", target = ".")
    SimpleEventDetails eventEntityToSimple(EventTaskSubjectTeachersEntities entities);

    EmptyEventDetails eventEntityToEmpty(EventTaskSubjectTeachersEntities entities);

    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "details", target = ".", qualifiedByName = "mapDetails")
    EventEntity eventToEventEntity(Event domain);

    @InheritInverseConfiguration
    EventEntity eventEntityToEvent(Event domain);

    default List<Event> entityToDomain(@NonNull List<EventTaskSubjectTeachersEntities> eventTaskSubjectTeachersEntities) {
        return eventTaskSubjectTeachersEntities.stream()
                .map(this::eventEntityToEvent)
                .collect(Collectors.toList());
    }

    @Mapping(source = "groupEntity", target = "group")
    @Mapping(source = "eventEntity", target = ".")
    @Mapping(target = "details", qualifiedByName = "mapDetails", source = ".")
    Event eventEntityToEvent(EventTaskSubjectTeachersEntities eventEntity);

    @Named("mapTeacherList")
    default List<String> mapTeacherList(@NotNull List<User> teachers) {
        return teachers.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Override
    List<EventEntity> docToEntity(List<EventDoc> doc);

    @Override
    List<EventDoc> entityToDoc(List<EventEntity> entity);

    default EventDoc domainToDoc(Event domain) {
        EventDoc eventDoc = eventToEventDoc(domain);
        eventDoc.setEventDetailsDoc(detailsToDetailsDoc(domain.getDetails()));
        return eventDoc;
    }

    @Mapping(source = "group.id", target = "groupId")
    EventDoc eventToEventDoc(Event domain);

    default List<EventDoc> domainToDoc(@NonNull List<Event> docs) {
        return docs.stream()
                .map(this::domainToDoc)
                .collect(Collectors.toList());
    }

    default EventDetailsDoc detailsToDetailsDoc(@NonNull EventDetails eventDetails) {
        if (eventDetails.getType().equals(EventEntity.TYPE.LESSON))
            return lessonToDetailsDoc((Lesson) eventDetails);
        else if (eventDetails.getType().equals(EventEntity.TYPE.SIMPLE))
            return simpleToDetailsDoc((SimpleEventDetails) eventDetails);
        else if (eventDetails.getType().equals(EventEntity.TYPE.EMPTY))
            return emptyToDetailsDoc(eventDetails);
        throw new IllegalArgumentException();
    }

    @Mapping(source = "teachers", target = "teacherIds", qualifiedByName = "mapTeacherList")
    @Mapping(source = "subject.id", target = "subjectId")
    EventDetailsDoc lessonToDetailsDoc(Lesson eventDetails);

    EventDetailsDoc simpleToDetailsDoc(SimpleEventDetails eventDetails);

    EventDetailsDoc emptyToDetailsDoc(EventDetails eventDetails);

    @Mapping(source = "date", target = "date", qualifiedByName = "toUTC")
    @Mapping(source = "eventDetailsDoc", target = ".")
    @Override
    EventEntity docToEntity(EventDoc doc);

    @Named("toUTC")
    default Date toUTC(Date date) {
        return DateFormatUtil.convertDateToDateUTC(date);
    }

    @Override
    EventDoc entityToDoc(EventEntity entity);

    default List<TeacherEventCrossRef> lessonEntitiesToTeacherLessonCrossRefEntities(@NotNull List<EventEntity> lessonEntities) {
        return lessonEntities.stream()
                .flatMap((Function<EventEntity, Stream<TeacherEventCrossRef>>) lessonEntity -> lessonEntity.getTeacherIds()
                        .stream()
                        .map(id -> new TeacherEventCrossRef(lessonEntity.getId(), id)))
                .collect(Collectors.toList());
    }
}
