package com.denchic45.widget.calendar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Week {

    private List<Date> dayOfWeekList;
    private int selectedDay;

    public Week(List<Date> dayOfWeekList, int selectedDay) {
        this.dayOfWeekList = dayOfWeekList;
        this.selectedDay = selectedDay;
    }

    public List<Date> getDayOfWeekList() {
        return dayOfWeekList;
    }

    public void setDayOfWeekList(List<Date> dayOfWeekList) {
        this.dayOfWeekList = dayOfWeekList;
    }

    public Date getDate(int position) {
        return dayOfWeekList.get(position);
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(int selectedDay) {
        this.selectedDay = selectedDay;
    }

    public void findAndSetCurrentDay() {
        selectedDay = findCurrentDay();
    }

    private int findCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        Calendar calendarToday =(Calendar) calendar.clone();
        for (int i = 0; i < dayOfWeekList.size(); i++) {
            calendar.setTime(dayOfWeekList.get(i));
            if (calendar.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH)) {
                return i;
            }
        }
        return -1;
    }
}
