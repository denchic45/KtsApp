package com.denchic45.kts.data.local.model

import com.denchic45.kts.GroupEntity
import com.denchic45.kts.UserEntity

class GroupWithCuratorAndStudentsEntity(
    val groupEntity: GroupEntity,
    val curatorEntity: UserEntity,
    val studentEntities: List<UserEntity>
)