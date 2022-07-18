package com.denchic45.kts.data.local.model

import com.denchic45.kts.GroupEntity
import com.denchic45.kts.SpecialtyEntity
import com.denchic45.kts.UserEntity

class GroupWithCuratorAndSpecialtyEntities(
    val groupEntity: GroupEntity,
    val curatorEntity: UserEntity,
    val specialtyEntity: SpecialtyEntity
)