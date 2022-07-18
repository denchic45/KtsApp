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

    fun observe(id: String): Flow<GroupWithCuratorAndSpecialtyEntities?> {
        return queries.getWithCuratorAndSpecialtyById(
            id,
            mapperOfGroupWithCuratorAndSpecialtyEntities
        )
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
        return queries.getWithCuratorAndSpecialtyByStudentId(
            userId,
            mapperOfGroupWithCuratorAndSpecialtyEntities
        )
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }

    fun getByCuratorId(userId: String): Flow<GroupWithCuratorAndSpecialtyEntities> {
        return queries.getWithCuratorAndSpecialtyByCuratorId(
            userId,
            mapperOfGroupWithCuratorAndSpecialtyEntities
        )
            .asFlow()
            .mapToOne(Dispatchers.IO)
    }

    private val mapperOfGroupWithCuratorAndSpecialtyEntities: (group_id: String, group_name: String, curator_id: String, course: Long, specialty_id: String, headman_id: String, timestamp: Long, user_id: String, first_name: String, surname: String, patronymic: String?, user_group_id: String, role: String, email: String?, photo_url: String, gender: Int, admin: Boolean, generated_avatar: Boolean, timestamp_: Long, specialty_id_: String, name: String) -> GroupWithCuratorAndSpecialtyEntities =
        { group_id, group_name, curator_id, course, specialty_id, headman_id, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_, specialty_id_, name ->
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
}