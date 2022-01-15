package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.data.model.EntityModel
import java.util.*

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    var id: String,
    var firstName: String,
    var surname: String,
    @ColumnInfo(name = "user_group_id")
    var groupUuid: String? = null,
    var role: String,
    var phoneNum: String,
    var email: String? = null,
    var photoUrl: String,
    var gender: Int = 0,
    var admin: Boolean = false,
    var patronymic: String? = null,
    var generatedAvatar: Boolean = false,
    @ColumnInfo(name = "user_timestamp")
    @field:TypeConverters(TimestampConverter::class) var timestamp: Date
) : EntityModel {

    @get:Ignore
    val fullName: String
        get() = firstName + surname
}