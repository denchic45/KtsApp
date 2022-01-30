package com.denchic45.kts.data.repository

import com.denchic45.kts.data.dao.GroupDao
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.UserEntity
import java.util.stream.Collectors

interface IGroupRepository {

    val userMapper: UserMapper
    val groupDao: GroupDao
    val specialtyDao: SpecialtyDao
    val groupMapper: GroupMapper
    val specialtyMapper: SpecialtyMapper
    val userDao: UserDao

    suspend fun saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc: GroupDoc) {
        upsertUsersOfGroup(groupDoc)
        groupDao.upsert(groupMapper.docToEntity(groupDoc))
        specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc.specialty))
    }

     suspend fun upsertUsersOfGroup(groupDoc: GroupDoc) {
        val allUsersEntity = userMapper.docToEntity(groupDoc.allUsers)
        userDao.upsert(allUsersEntity)
        val availableUsers = allUsersEntity.map { obj: UserEntity -> obj.id }
        userDao.deleteMissingStudentsByGroup(availableUsers, groupDoc.id)
    }
}