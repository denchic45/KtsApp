package com.denchic45.kts.data.model.mapper;


import static com.denchic45.kts.TestUtils.createCurator;
import static com.denchic45.kts.TestUtils.createStudent1;
import static com.denchic45.kts.TestUtils.createStudent2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.denchic45.kts.data.model.domain.Group;
import com.denchic45.kts.data.model.domain.Specialty;
import com.denchic45.kts.data.model.domain.User;
import com.denchic45.kts.data.model.firestore.EventDetailsDoc;
import com.denchic45.kts.data.model.firestore.EventDoc;
import com.denchic45.kts.data.model.room.EventEntity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

class EventMapperTest {

    EventMapper mapper = new EventMapperImpl();

    @Test
    void testMapDomainToEntity() {
        User curator = new User("uuid", "Марина", "Иванова", "", "", User.TEACHER, "+71234567890", "marina@mail.ru", "", new Date(), 1, true, false);
        Group group = new Group("uuidOfGroup", "group", 1, new Specialty("uuid", "specialty"), curator);
        List<User> teachers = Arrays.asList(
                new User("uuid", "Иван", "Иванов", "", "", User.TEACHER, "+71234567890", "ivan@mail.ru", "", new Date(), 1, true, false),
                new User("uuid", "Петр", "Петров", "", "", User.TEACHER, "+71234567890", "petr@@mail.ru", "", new Date(), 1, true, false)
        );
//        Event event = new Event("", group, new Date(), 1, new Date(), "23", new Lesson(createSubject(), null, teachers));
//        EventEntity eventEntity = mapper.domainToEntity(event);

//        assertEquals(EventEntity.TYPE.LESSON, eventEntity.getType());
//        assertEquals("uuidOfGroup", eventEntity.getGroupId());
//        assertEquals("uuidOfSubject", eventEntity.getSubjectUuid());
//        assertEquals("23", eventEntity.getRoom());
    }

    @Test
    void testMapEntityToDomain() {
//        List<String> teachersUuid = Arrays.asList("uuidOfTeacher1", "uuidOfTeacher2");
//        List<UserEntity> teachers = Arrays.asList(
//                new UserEntity("Иван", "Иванов", User.TEACHER, "+71234567890", "ivan@mail.ru", "", 1, false),
//                new UserEntity("Петр", "Петров", User.TEACHER, "+71234567890", "petr@@mail.ru", "", 1, false)
//        );
//        EventEntity eventEntity = new EventEntity("uuid", new Date(), 2, new Date(), "23", "uuidOfSubject", teachersUuid, "uuidOfGroup", EventEntity.TYPE.LESSON);
//        TaskEntity taskEntity = new TaskEntity("uuidOfHomework", "task", "");
//        SubjectEntity subjectEntity = new SubjectEntity("uuidOfSubject", "Math", "", " red");
//        GroupEntity groupEntity = new GroupEntity("uuidOfGroup", "PKS", 2, "uuidOfSpecialty", new Date());
//        EventTaskSubjectTeachersEntities entity = new EventTaskSubjectTeachersEntities(eventEntity, taskEntity, subjectEntity, teachers, createGroupWithCuratorAndSpecialtyEntity());
//
//        Event event = mapper.eventEntityToEvent(entity);
//
//        assertEquals(EventEntity.TYPE.LESSON, eventEntity.getType());
//        assertEquals("uuidOfSubject", ((Lesson) event.getDetails()).getSubject().getUuid());
//        assertEquals("23", event.getRoom());
//        assertEquals("task", ((Lesson) event.getDetails()).getTask().getTitle());
//        assertEquals("uuidOfGroup", (event.getGroup().getUuid()));
//
//        EventEntity eventEntity2 = new EventEntity("uuid", new Date(), 2, new Date(), "23", "uuidOfGroup", EventEntity.TYPE.SIMPLE, "Обед", "yellow");
//        EventTaskSubjectTeachersEntities entities2 = new EventTaskSubjectTeachersEntities(eventEntity2, taskEntity, subjectEntity, teachers, createGroupWithCuratorAndSpecialtyEntity());
//
//        Event event2 = mapper.eventEntityToEvent(entities2);
//
//        assertEquals(EventEntity.TYPE.SIMPLE, event2.getType());
//        assertEquals("23", event2.getRoom());
//        assertEquals("Обед", ((SimpleEventDetails) event2.getDetails()).getName());
//        assertEquals("yellow", ((SimpleEventDetails) event2.getDetails()).getColor());
    }



    @Test
    void testMapDocToEntity() {
        EventDoc eventDoc = new EventDoc("uuidOfEvent", new Date(), 1, new Date(), "23", "uuidOfGroup", new EventDetailsDoc("uuidOfSubject", null, Arrays.asList("uuid1", "uuid2"), null, null, null, "LESSON"));

        EventEntity eventEntity = mapper.docToEntity(eventDoc);

        assertEquals(EventEntity.TYPE.LESSON, eventEntity.getType());
        assertEquals("uuidOfGroup", eventEntity.getGroupId());
        assertEquals("uuidOfSubject", eventEntity.getSubjectUuid());
        assertEquals("23", eventEntity.getRoom());
        assertEquals(eventDoc.getEventDetailsDoc().getTeacherUuidList().get(0), eventEntity.getTeacherUuidList().get(0));
    }

    @Test
    void testEmptyListOfTeacher() {
//        List<User> teachers = Arrays.asList(
//                new User( "Иван", "Иванов", User.TEACHER, "+71234567890", "ivan@mail.ru", "", "", 1, false),
//                new User( "Петр", "Петров", User.TEACHER, "+71234567890", "petr@@mail.ru", "", "", 1, false)
//        );
        EventDoc eventDoc = new EventDoc("uuidOfEvent", new Date(), 1, new Date(), "23", "uuidOfGroup", new EventDetailsDoc("uuidOfSubject", null, Arrays.asList("uuid1", "uuid2"), null, null, null, "LESSON"));
        EventEntity eventEntity = mapper.docToEntity(eventDoc);
        assertNotNull(eventEntity.getTeacherUuidList());
    }

    @Test
    void testMapLessonToEventDoc() {
        Group group = new Group("uuidOfGroup", "group", 1, new Specialty("uuid", "specialty"), createCurator());
        List<User> teachers = Arrays.asList(
                createStudent1(),
                createStudent2()
        );

//        Event event = new Event("",group, new Date(), 1,new Date(),"23", new Lesson(createSubject(), null,teachers));
//
//        EventDoc eventDoc = mapper.domainToDoc(event);
//
//        assertEquals("LESSON", eventDoc.getEventDetailsDoc().getType());
//        assertEquals("uuidOfSubject", eventDoc.getEventDetailsDoc().getSubjectId());
//        assertEquals("uuidOfGroup", eventDoc.getGroupId());
//        assertEquals("23", eventDoc.getRoom());
    }


}