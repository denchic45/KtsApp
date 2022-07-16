package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.denchic45.kts.domain.EntityModel
import java.util.*

@Entity(tableName = "group")
class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    val id: String,
    @ColumnInfo(name = "group_name")
    val name: String,
    @ColumnInfo(name = "curator_id")
    val curatorId: String,
    val course: Int = 0,
    @ColumnInfo(name = "specialty_id")
    val specialtyId: String,
    @ColumnInfo(name = "headman_id")
    val headmanId: String?,
    @ColumnInfo(name = "group_timestamp")
    @field:TypeConverters(TimestampConverter::class)
    val timestamp: Date
) : EntityModel