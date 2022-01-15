package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Relation
import com.denchic45.kts.data.model.EntityModel

data class GroupWithCuratorAndSpecialtyEntity(
    @Embedded
    var groupEntity: GroupEntity,
    @Relation(parentColumn = "curator_id", entityColumn = "user_id")
    var curatorEntity: UserEntity,
    @Relation(parentColumn = "specialty_id", entityColumn = "specialty_id")
    var specialtyEntity: SpecialtyEntity
) : EntityModel