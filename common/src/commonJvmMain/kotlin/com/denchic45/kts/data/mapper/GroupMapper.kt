package com.denchic45.kts.data.mapper

import com.denchic45.kts.GetStudentsWithCuratorByGroupId
import com.denchic45.kts.domain.model.GroupCurator
import com.denchic45.kts.domain.model.GroupMembers
import com.denchic45.kts.domain.model.GroupStudent

fun List<GetStudentsWithCuratorByGroupId>.toGroupMembers(): GroupMembers {
    return GroupMembers(
        groupId = first().group_id,
        curator = first { it.curator_id == it.user_id }.let { curatorEntity ->
            GroupCurator(
                id = curatorEntity.user_id,
                firstName = curatorEntity.first_name,
                surname = curatorEntity.surname,
                patronymic = curatorEntity.patronymic,
                groupId = curatorEntity.user_group_id,
                photoUrl = curatorEntity.photo_url
            )
        },
        headmanId = first().headman_id,
        students = map {
            GroupStudent(
                id = it.user_id,
                firstName = it.first_name,
                surname = it.surname,
                patronymic = it.patronymic,
                groupId = it.group_id,
                photoUrl = it.photo_url
            )
        }
    )
}