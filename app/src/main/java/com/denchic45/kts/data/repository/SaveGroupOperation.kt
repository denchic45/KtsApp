package com.denchic45.kts.data.repository

import com.denchic45.kts.UserEntity
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.local.db.GroupLocalDataSource
import com.denchic45.kts.data.local.db.SpecialtyLocalDataSource
import com.denchic45.kts.data.local.db.UserLocalDataSource
import com.denchic45.kts.data.mapper.docsToEntities
import com.denchic45.kts.data.mapper.toEntity
import com.denchic45.kts.data.remote.model.GroupDoc
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.model.mapper.SpecialtyMapper

interface SaveGroupOperation {
    val groupLocalDataSource:GroupLocalDataSource
    val specialtyLocalDataSource:SpecialtyLocalDataSource
    val userLocalDataSource: UserLocalDataSource
    val dataBase: DataBase

    private suspend fun upsertUsersOfGroup(groupDoc: GroupDoc) {
        val allUsersEntity: List<UserEntity> = groupDoc.allUsers.docsToEntities()
        userLocalDataSource.upsert(allUsersEntity)
        val availableUsers = allUsersEntity.map { obj: UserEntity -> obj.user_id }
        userLocalDataSource.deleteMissingStudentsByGroup( groupDoc.id, availableUsers)
    }

    suspend fun saveGroup(groupDoc: GroupDoc) {
        groupLocalDataSource.upsert(groupDoc.toEntity())
        upsertUsersOfGroup(groupDoc)
        specialtyLocalDataSource.upsert(groupDoc.specialty.toEntity())
    }

    suspend fun saveGroups(
        groupDocs: List<GroupDoc>
    ) {
        for (groupDoc in groupDocs) {
            saveGroup(groupDoc)
        }
    }
}