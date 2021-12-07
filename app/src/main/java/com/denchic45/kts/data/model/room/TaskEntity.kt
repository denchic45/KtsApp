package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.data.model.EntityModel
import java.util.*

@Entity(tableName = "homework")
data class TaskEntity(
    @field:PrimaryKey
    @field:ColumnInfo(name = "uuid_homework")
    var uuid: String,
    var courseId:String,
    val name: String,
    val content: String,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_completion")
    val dateOfCompletion: Date,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_created")
    val dateOfCreated: Date,
    val completed: Boolean
) : EntityModel {

    @TypeConverters(TimestampConverter::class)
    var timestamp: Date? = null
}