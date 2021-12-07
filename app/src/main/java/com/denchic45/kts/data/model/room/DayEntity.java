package com.denchic45.kts.data.model.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Entity(tableName = "day")
public class DayEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "day_uuid")
    private String uuid;
    @TypeConverters(DateConverter.class)
    private Date date;
    @ColumnInfo(name = "group_uuid")
    private String groupUuid;

    @Ignore
    public DayEntity(@NotNull String uuid, Date date, String groupUuid) {
        this.uuid = uuid;
        this.date = date;
        this.groupUuid = groupUuid;
    }

    public DayEntity() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public @NotNull String getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull String uuid) {
        this.uuid = uuid;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }
}
