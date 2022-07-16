package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denchic45.kts.domain.EntityModel

@Entity(tableName = "specialty")
data class SpecialtyEntity(
    @PrimaryKey
    @ColumnInfo(name = "specialty_id")
    var id: String,
    var name: String
) : EntityModel