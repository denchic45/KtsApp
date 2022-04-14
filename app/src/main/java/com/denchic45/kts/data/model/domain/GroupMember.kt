package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class GroupMembers(
    val groupId: String?,
    val curator: GroupCurator,
    val students: List<GroupStudent>,
    val headmanId: String?
) {
    fun isHeadman(member: GroupMember): Boolean {
        return headmanId == member.id
    }
}

 interface GroupMember : DomainModel {
     override val id: String
     val firstName: String
     val surname: String
     val patronymic: String?
     val groupId: String?
     val photoUrl: String

    val fullName: String
        get() = "$firstName $surname"
}

data class GroupStudent(
    override val id: String,
    override val firstName: String,
    override val surname: String,
    override val patronymic: String?,
    override val groupId: String?,
    override val photoUrl: String,
):GroupMember

data class GroupCurator(
    override val id: String,
    override val firstName: String,
    override val surname: String,
    override val patronymic: String?,
    override val groupId: String?,
    override val photoUrl: String,
):GroupMember