package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Relation
import com.denchic45.kts.data.model.EntityModel

data class GroupWithCuratorAndSpecialtyEntity(
    @Embedded
    val groupEntity: GroupEntity,
    @Relation(parentColumn = "curator_id", entityColumn = "user_id")
    val curatorEntity: UserEntity,
    @Relation(parentColumn = "specialty_id", entityColumn = "specialty_id")
    val specialtyEntity: SpecialtyEntity
) : EntityModel

data class GroupWithCuratorAndStudentsEntity(
    @Embedded
    val groupEntity: GroupEntity,
    @Relation(parentColumn = "curator_id", entityColumn = "user_id")
    val curatorEntity: UserEntity,
    @Relation(parentColumn = "group_id", entityColumn = "user_group_id")
    val studentEntities: List<UserEntity>
) : EntityModel