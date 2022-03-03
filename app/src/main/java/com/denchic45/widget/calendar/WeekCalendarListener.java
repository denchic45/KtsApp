package com.denchic45.widget.calendar;

import com.denchic45.widget.calendar.model.WeekItem;

import java.time.LocalDate;

public interface WeekCalendarListener {

    void onDaySelect(LocalDate date);

    void onWeekSelect(WeekItem weekItem);

    interface OnLoadListener {

        void onWeekLoad(WeekItem weekItem);
    }
}
