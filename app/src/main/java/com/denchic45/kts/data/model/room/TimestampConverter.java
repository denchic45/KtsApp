package com.denchic45.kts.data.model.room;

import androidx.room.TypeConverter;

import java.util.Date;

public class TimestampConverter {

    @TypeConverter
    public long toLong(Date date) {
        if (date == null)
            return System.currentTimeMillis();
        return date.getTime();
    }

    @TypeConverter
    public Date toDate(long timestamp) {
        return new Date(timestamp);
    }

}