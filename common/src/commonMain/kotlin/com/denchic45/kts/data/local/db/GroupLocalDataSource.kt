package com.denchic45.kts.data.local.db

import com.denchic45.kts.*
import com.denchic45.kts.data.local.model.GroupWithCuratorAndSpecialtyEntities
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GroupLocalDataSource(db: AppDatabase) {

    private val queries: GroupEntityQueries = db.groupEntityQueries

    suspend fun upsert(groupEntity: GroupEntity) = withContext(Dispatchers.IO) {
        queries.upsert(groupEntity)
    }

    suspend fun upsert(groupEntities: List<GroupEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            groupEntities.forEach { queries.upsert(it) }
        }
    }

    fun observe(id: String): Flow<GroupWithCuratorAndSpecialtyEntities?> {
        return queries.getWithCuratorAndSpecialtyById(
            id
        ) { group_id, group_name, curator_id, course, specialty_id, headman_id, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_, specialty_id_, name ->
            GroupWithCuratorAndSpecialtyEntities(
                GroupEntity(
                    group_id,
                    group_name,
                    curator_id,
                    course,
                    specialty_id,
                    headman_id,
                    timestamp
                ),
                UserEntity(
                    user_id,
                    first_name,
                    surname,
                    patronymic,
                    user_group_id,
                    role,
                    email,
                    photo_url,
                    gender,
                    admin,
                    generated_avatar,
                    timestamp_
                ),
                SpecialtyEntity(specialty_id_, name)
            )
        }
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    }

    fun getNameById(groupId: String): Flow<String> {
        return queries.getNameById(groupId).asFlow().mapToOne(Dispatchers.IO)
    }

    fun observeIsExist(id: String): Flow<Boolean> {
        return queries.isExist(id).asFlow().mapToOne(Dispatchers.IO)
    }

    suspend fun isExist(id: String): Boolean = withContext(Dispatchers.IO) {
        queries.isExist(id).executeAsOne()
    }

    suspend fun deleteById(groupId: String) = withContext(Dispatchers.IO) {
        queries.deleteById(groupId)
    }

    fun getByStudentId(userId: String): Flow<GroupWithCuratorAndSpecialtyEntities> {
        return queries.getWithCuratorAndSpecialtyByStudentId<GroupWithCuratorAndSpecialtyEntities>(
            userId
        ) { group_id, group_name, curator_id, course, specialty_id, headman_id, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_, specialty_id_, name ->
            GroupWithCuratorAndSpecialtyEntities(
                GroupEntity(
                    group_id,
                    group_name,
                    curator_id,
                    course.toInt(),
                    specialty_id,
                    headman_id,
                    timestamp
                ),
                UserEntity(
                    user_id,
                    first_name,
                    surname,
                    patronymic,
                    user_group_id,
                    role,
                    email,
                    photo_url,
                    gender,
                    admin,
                    generated_avatar,
                    timestamp_
                ),
                SpecialtyEntity(specialty_id_, name)
            )
        }
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }

    fun getByCuratorId(userId: String): Flow<GroupWithCuratorAndSpecialtyEntities> {
        return queries.getWithCuratorAndSpecialtyByCuratorId<GroupWithCuratorAndSpecialtyEntities>(
            userId
        ) { group_id, group_name, curator_id, course, specialty_id, headman_id, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_, specialty_id_, name ->
            GroupWithCuratorAndSpecialtyEntities(
                GroupEntity(
                    group_id,
                    group_name,
                    curator_id,
                    course.toInt(),
                    specialty_id,
                    headman_id,
                    timestamp
                ),
                UserEntity(
                    user_id,
                    first_name,
                    surname,
                    patronymic,
                    user_group_id,
                    role,
                    email,
                    photo_url,
                    gender,
                    admin,
                    generated_avatar,
                    timestamp_
                ),
                SpecialtyEntity(specialty_id_, name)
            )
        }
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }
}