package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.denchic45.kts.data.model.EntityModel;

@Entity(tableName = "teacher_event", primaryKeys = {"eventUuid", "uuid_user"},
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class,
                        parentColumns = "uuid_user",
                        childColumns = "uuid_user",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true),
                @ForeignKey(entity = EventEntity.class,
                        parentColumns = "eventUuid",
                        childColumns = "eventUuid",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true)
        })
public class TeacherEventCrossRef implements EntityModel {

    @ColumnInfo(index = true)
    @NonNull
    public String eventUuid;
    @ColumnInfo(index = true, name = "uuid_user")
    @NonNull
    public String teacherUuid;

    public TeacherEventCrossRef(@NonNull String eventUuid, @NonNull String teacherUuid) {
        this.eventUuid = eventUuid;
        this.teacherUuid = teacherUuid;
    }
}
