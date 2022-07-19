package com.denchic45.kts.data.mapper

import com.denchic45.kts.GetStudentsWithCuratorByGroupId
import com.denchic45.kts.GroupEntity
import com.denchic45.kts.data.local.model.GroupWithCuratorAndSpecialtyEntities
import com.denchic45.kts.data.remote.model.GroupDoc
import com.denchic45.kts.domain.model.*

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

@Deprecated("")
fun GroupDoc.toEntity() = GroupEntity(
    group_id = id,
    group_name = name,
    curator_id = curator.id,
    course = course,
    specialty_id = specialty.id,
    headman_id = headmanId!!,
    timestamp = timestamp!!.time
)

@Deprecated("")
fun List<GroupDoc>.docsToEntities() = map(GroupDoc::toEntity)

fun GroupWithCuratorAndSpecialtyEntities.toDomain() = Group(
    id = groupEntity.group_id,
    name = groupEntity.group_name,
    course = groupEntity.course,
    specialty = specialtyEntity.toDomain(),
    curator = curatorEntity.toDomain()
)

fun GroupDoc.toDomain() = Group(
    id = id,
    name = name,
    course = course,
    specialty = specialty.toDomain(),
    curator = curator.toDomain()
)

fun List<GroupDoc>.docsToDomains() = map(GroupDoc::toDomain)

fun GroupDoc.toGroupHeader() = GroupHeader(
    id = id,
    name = name,
    specialtyId = specialty.id,
)

fun List<GroupDoc>.docsToGroupHeaders() = map(GroupDoc::toGroupHeader)

fun Group.domainToMap(): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    map["id"] = id
    map["name"] = name
    map["course"] = course
    map["specialtyId"] = specialty.id
//    map["timestamp"] = FieldValue.serverTimestamp()
    map["specialty"] = specialty
    map["curator"] = curator
    return map
}