package com.denchic45.kts.data.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.denchic45.kts.data.model.EntityModel

@Entity(tableName = "section")
data class SectionEntity(
    @PrimaryKey
    val uuid: String,
    val name: String,
    val courseUuid: String
):EntityModel
