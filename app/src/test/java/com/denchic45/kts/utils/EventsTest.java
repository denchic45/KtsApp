package com.denchic45.kts.utils;

import com.denchic45.kts.data.model.domain.Event;
import com.denchic45.kts.data.model.domain.Lesson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class EventsTest {

//    @Test
//    void addMissingEmptyLessons() {
//        List<Event> lessons = new ArrayList<>(Arrays.asList(
//                new Event(null, "1", null, 0, null, new Date()),
//                new Lesson(null, "2", null, 1, null, new Date()),
//                new Lesson(null, "3", null, 3, null, new Date()),
//                new Lesson(null, "5", null, 5, null, new Date()),
//                new Lesson(null, "6", null, 6, null, new Date())
//                ));
//        Events.addMissingEmptyLessons(lessons);
//        lessons.forEach(lesson -> System.out.println(lesson.getOrder() + "     " + lesson.getRoom()));
//    }
//
//    @Test
//    void shiftOrders() {
//        List<Event> lessons = new ArrayList<>(Arrays.asList(
//                new Lesson(null, "1", null, 2, null, new Date()),
//                new Lesson(null, "2", null, 3, null, new Date()),
//                new Lesson(null, "3", null, 4, null, new Date()),
//                new Lesson(null, "4", null, 5, null, new Date())
//        ));
//        Events.shiftOrders(3, lessons);
//        lessons.forEach(lesson -> System.out.println(lesson.getRoom() + " " + lesson.getOrder()));
//    }
}