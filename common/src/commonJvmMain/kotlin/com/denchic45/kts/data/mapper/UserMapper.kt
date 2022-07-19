package com.denchic45.kts.data.mapper

import com.denchic45.kts.UserEntity
import com.denchic45.kts.data.remote.model.UserDoc
import com.denchic45.kts.domain.model.User
import java.util.*

fun User.toEntity() = UserEntity(
    id,
    firstName,
    surname,
    patronymic,
    groupId ?: "",
    role.toString(),
    email,
    photoUrl,
    gender,
    admin,
    generatedAvatar,
    timestamp!!.time
)

fun List<User>.domainsToEntities() = this.map(User::toEntity)

fun UserEntity.toDomain() = User(
    user_id,
    first_name,
    surname,
    patronymic,
    user_group_id,
    User.Role.valueOf(role),
    email,
    photo_url,
    Date(timestamp),
    gender,
    admin,
    generated_avatar,
)

fun List<UserEntity>.toDomain() = map { }

@Deprecated("")
fun UserDoc.toEntity() = UserEntity(
    id,
    firstName,
    surname,
    patronymic,
    groupId ?: "",
    role.toString(),
    email,
    photoUrl,
    gender,
    admin,
    generatedAvatar,
    timestamp!!.time
)

@Deprecated("")
fun List<UserDoc>.docsToEntities() = this.map(UserDoc::toEntity)

@Deprecated("")
fun User.toDoc() = UserDoc(
    id = id,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    groupId = groupId,
    role = role,
    email = email,
    photoUrl = photoUrl,
    timestamp = timestamp,
    gender = gender,
    generatedAvatar = generatedAvatar,
    admin = admin
)

@Deprecated("")
fun List<User>.domainsToDocs() = this.map(User::toDoc)

fun UserDoc.toDomain() = User(
    id = id,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    groupId = groupId,
    role = role,
    email = email,
    photoUrl = photoUrl,
    timestamp = timestamp!!,
    gender = gender,
    generatedAvatar = generatedAvatar,
    admin = admin,
)

fun List<UserDoc>.docsToDomains() = this.map(UserDoc::toDomain)

fun Map<String, Any>.mapToUserEntity() = UserEntity(
    user_id = get("id") as String,
    first_name = get("firstName") as String,
    surname = get("surname") as String,
    patronymic = get("patronymic") as String,
    user_group_id = get("groupId") as String,
    role = get("role") as String,
    email = get("email") as String,
    timestamp = (get("timestamp") as Date).time,
    gender = get("gender") as Int,
    admin = get("admin") as Boolean,
    photo_url = get("photoUrl") as String,
    generated_avatar = get("generatedAvatar") as Boolean,
)

fun List<Map<String, Any>>.mapsToUserEntities() = map { it.mapToUserEntity() }

fun Map<String, Any>.mapToUser() = User(
    id = get("id") as String,
    firstName = get("firstName") as String,
    surname = get("surname") as String,
    patronymic = get("patronymic") as String,
    groupId = get("groupId") as String,
    role = User.Role.valueOf((get("role") as String)),
    email = get("email") as String,
    photoUrl = get("photoUrl") as String,
    timestamp = get("timestamp") as Date,
    gender = get("gender") as Int,
    generatedAvatar = get("generatedAvatar") as Boolean,
    admin = get("admin") as Boolean
)

fun List<Map<String, Any>>.mapsToUsers() = map { it.mapToUser() }