package com.denchic45.kts.data.repository

import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.dao.GroupDao
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.UserEntity

interface SaveGroupOperation {

    val userMapper: UserMapper
    val groupDao: GroupDao
    val specialtyDao: SpecialtyDao
    val groupMapper: GroupMapper
    val specialtyMapper: SpecialtyMapper
    val userDao: UserDao
    val dataBase: DataBase

    private suspend fun upsertUsersOfGroup(groupDoc: GroupDoc) {
        val allUsersEntity = userMapper.docToEntity(groupDoc.allUsers)
        userDao.upsert(allUsersEntity)
        val availableUsers = allUsersEntity.map { obj: UserEntity -> obj.id }
        userDao.deleteMissingStudentsByGroup(availableUsers, groupDoc.id)
    }

    suspend fun saveGroup(groupDoc: GroupDoc) {
        groupDao.upsert(groupMapper.docToEntity(groupDoc))
        upsertUsersOfGroup(groupDoc)
        specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc.specialty))
    }

    suspend fun saveGroups(
        groupDocs: List<GroupDoc>
    ) {
        for (groupDoc in groupDocs) {
            saveGroup(groupDoc)
        }
    }
}