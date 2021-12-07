package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.denchic45.kts.data.model.EntityModel;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;

@Entity(tableName = "group")
public class GroupEntity implements EntityModel {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "uuid_group")
    protected String uuid;
    @ColumnInfo(name = "name_group")
    private String name;
    @ColumnInfo(name = "uuid_curator")
    private String curatorUuid;
    private int course;
    private String specialtyUuid;
    @ColumnInfo(name = "timestamp_group")
    @TypeConverters(TimestampConverter.class)
    private Date timestamp;

    @Ignore
    public GroupEntity(@Nonnull String uuid, String name, int course, String specialtyUuid, Date timestamp) {
        this.uuid = uuid;
        this.name = name;
        this.course = course;
        this.specialtyUuid = specialtyUuid;
        this.timestamp = timestamp;
    }

    @Ignore
    public GroupEntity(String name, int course, String specialtyUuid, Date timestamp) {
        uuid = UUID.randomUUID().toString();
        this.name = name;
        this.course = course;
        this.specialtyUuid = specialtyUuid;
        this.timestamp = timestamp;
    }

    public GroupEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public String getSpecialtyUuid() {
        return specialtyUuid;
    }

    public void setSpecialtyUuid(String specialtyUuid) {
        this.specialtyUuid = specialtyUuid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCuratorUuid() {
        return curatorUuid;
    }

    public void setCuratorUuid(String curatorUuid) {
        this.curatorUuid = curatorUuid;
    }
}
