package com.denchic45.kts.data.model.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.denchic45.kts.data.model.EntityModel;

@Entity(tableName = "group_course", primaryKeys = {"group_id","course_id"},
        foreignKeys = {
                @ForeignKey(entity = GroupEntity.class,
                        parentColumns = "group_id",
                        childColumns = "group_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true),
                @ForeignKey(entity = CourseEntity.class,
                        parentColumns = "course_id",
                        childColumns = "course_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE,
                        deferred = true)
        })
public class GroupCourseCrossRef implements EntityModel {
    public GroupCourseCrossRef(@NonNull String groupId, @NonNull String courseUuid) {
        this.groupId = groupId;
        this.courseUuid = courseUuid;
    }

    @ColumnInfo(index = true, name = "group_id")
    @NonNull
    public String groupId;
    @ColumnInfo(index = true, name = "course_id")
    @NonNull
    public String courseUuid;
}
