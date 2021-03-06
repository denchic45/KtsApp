package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denchic45.kts.domain.EntityModel

@Entity(tableName = "subject")
data class SubjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "subject_id")
    var id: String,
    var name: String,
    var iconUrl: String,
    var colorName: String
) : EntityModel {
    private constructor() : this(
        "",
        "",
        "",
        ""
    )
}