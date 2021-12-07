package com.denchic45.kts.data.model.room;

import androidx.room.TypeConverter;

import com.denchic45.kts.utils.DateFormatUtil;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public String toString(Date date) {
        return DateFormatUtil.convertDateToString(date, DateFormatUtil.yyy_MM_dd);
    }

    @TypeConverter
    public Date toDate(String date) {
        return DateFormatUtil.convertStringToDateUTC(date, DateFormatUtil.yyy_MM_dd);
    }

}