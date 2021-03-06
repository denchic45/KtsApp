package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.GroupEntity
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {

    @Transaction
    @Query("SELECT * FROM `group` WHERE group_id=:id")
    abstract fun observe(id: String): Flow<GroupWithCuratorAndSpecialtyEntity?>

//    @Transaction
//    @Query("SELECT * FROM `group` WHERE group_id=:id")
//    abstract fun get(id: String): GroupWithCuratorAndSpecialtyEntity?

//    @Query("DELETE FROM `group` WHERE group_id NOT IN(:availableGroups)")
//    abstract fun deleteMissing(availableGroups: String)

    @Query("SELECT group_name FROM `group` WHERE group_id =:groupId")
    abstract fun getNameById(groupId: String): Flow<String>

//    @Query("SELECT group_timestamp FROM `group` WHERE group_id =:id")
//    abstract fun getTimestampById(id: String): Long

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :id)")
    abstract fun observeIsExist(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :id)")
    abstract suspend fun isExist(id: String): Boolean

    @Query("DELETE FROM `group` WHERE group_id =:groupId")
    abstract fun deleteById(groupId: String)

    @Query("SELECT * FROM `group` g JOIN user u ON g.group_id = u.user_group_id WHERE u.user_id =:userId ")
    abstract fun getByStudentId(userId: String): Flow<GroupWithCuratorAndSpecialtyEntity>

    @Query("SELECT * FROM `group` WHERE curator_id =:userId")
    abstract fun getByCuratorId(userId: String): Flow<GroupWithCuratorAndSpecialtyEntity>

//    @Query("DELETE FROM `group` WHERE group_id NOT IN(SELECT g.group_id FROM `group` g INNER JOIN group_course gc ON gc.group_id == g.group_id)")
//    abstract fun deleteUnrelatedByCourse()
}