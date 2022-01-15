package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.EntityModel
import java.util.*

@Entity(tableName = "group")
class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    var id: String,
    @ColumnInfo(name = "group_name")
    var name: String,
    @ColumnInfo(name = "curator_id")
    var curatorId: String,
    var course: Int = 0,
    @ColumnInfo(name = "specialty_id")
    var specialtyId: String,
    @ColumnInfo(name = "group_timestamp")
    @field:TypeConverters(TimestampConverter::class)
    var timestamp: Date
) : EntityModel