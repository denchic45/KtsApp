package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.denchic45.kts.data.model.EntityModel;

@Entity(tableName = "group_course", primaryKeys = {"uuid_group","uuid_course"},
        foreignKeys = {
                @ForeignKey(entity = GroupEntity.class,
                        parentColumns = "uuid_group",
                        childColumns = "uuid_group",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true),
                @ForeignKey(entity = CourseEntity.class,
                        parentColumns = "uuid_course",
                        childColumns = "uuid_course",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true)
        })
public class GroupCourseCrossRef implements EntityModel {
    public GroupCourseCrossRef(@NonNull String groupUuid, @NonNull String courseUuid) {
        this.groupUuid = groupUuid;
        this.courseUuid = courseUuid;
    }

    @ColumnInfo(index = true, name = "uuid_group")
    @NonNull
    public String groupUuid;
    @ColumnInfo(index = true, name = "uuid_course")
    @NonNull
    public String courseUuid;
}
