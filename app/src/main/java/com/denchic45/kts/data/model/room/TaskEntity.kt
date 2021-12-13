package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.data.model.EntityModel
import java.util.*

@Entity(tableName = "task")
data class TaskEntity(
    @field:PrimaryKey
    @field:ColumnInfo(name = "uuid_homework")
    var uuid: String,
    var courseUuid:String,
    val name: String,
    val content: String,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_created")
    val createdDate: Date,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_updated")
    val updatedDate: Date,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_completion")
    val completionDate: Date,
) : EntityModel {

    @TypeConverters(TimestampConverter::class)
    var timestamp: Date? = null
}