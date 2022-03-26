package com.denchic45.kts;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LessonTimeCalculator {

    private static final int BREAK_TIME = 5;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private int orderLesson, lessonTime;

    public String getCalculatedTime(int orderLesson, int lessonTime) {
        this.orderLesson = orderLesson;
        this.lessonTime = lessonTime;
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 40);
        return getStartTime() + "-" + getEndTime();
    }

    @NotNull
    private String getStartTime() {
        calendar.add(Calendar.MINUTE, (lessonTime * orderLesson) + (orderLesson * BREAK_TIME));
        if (orderLesson == 1) {
            calendar.set(Calendar.HOUR, 8);
            calendar.set(Calendar.MINUTE, 30);
        } else if (orderLesson > 1) {
            calendar.add(Calendar.MINUTE, BREAK_TIME * 2);
        }
        dateFormat.format(calendar.getTime());
        return dateFormat.format(calendar.getTime());
    }

    @NotNull
    private String getEndTime() {
        if (orderLesson == 0) {
            calendar.add(Calendar.MINUTE, +40);
        } else {
            calendar.add(Calendar.MINUTE, +lessonTime);
        }
        if (orderLesson == 0) {
            calendar.add(Calendar.MINUTE, BREAK_TIME);
        }
        return dateFormat.format(calendar.getTime());
    }
}