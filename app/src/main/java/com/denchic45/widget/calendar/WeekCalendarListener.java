package com.denchic45.widget.calendar;

import com.denchic45.widget.calendar.model.Week;

import java.util.Date;

public interface WeekCalendarListener {

    void onDaySelect(Date date);

    void onWeekSelect(Week week);

    interface OnLoadListener {

        void onWeekLoad(Week week);
    }
}
