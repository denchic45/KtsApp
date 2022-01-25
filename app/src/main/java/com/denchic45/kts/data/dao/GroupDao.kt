package com.denchic45.kts.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.GroupEntity
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {
    @get:Query("SELECT * FROM `group`")
    abstract val allGroups: LiveData<List<GroupEntity?>?>?

    @Transaction
    @Query("SELECT * FROM `group` WHERE group_id=:id")
    abstract fun get(id: String): LiveData<GroupWithCuratorAndSpecialtyEntity>

    @Transaction
    @Query("SELECT * FROM `group` WHERE group_id=:id")
    abstract fun getSync(id: String): GroupWithCuratorAndSpecialtyEntity?

    @Query("DELETE FROM `group` WHERE group_id NOT IN(:availableGroups)")
    abstract fun deleteMissing(availableGroups: String)

    @Query("SELECT group_name FROM `group` WHERE group_id =:groupId")
    abstract fun getNameById(groupId: String): Flow<String>

    @Query("SELECT group_timestamp FROM `group` WHERE group_id =:id")
    abstract fun getTimestampById(id: String): Long

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :id)")
    abstract fun isExist(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :id)")
    abstract suspend fun isExistSync(id: String): Boolean

    @Query("DELETE FROM `group` WHERE group_id =:groupId")
    abstract fun deleteById(groupId: String)

    @Query("SELECT * FROM `group` g JOIN user u ON g.group_id = u.user_group_id WHERE u.user_id =:userId ")
    abstract fun getByStudentId(userId: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("SELECT * FROM `group` WHERE curator_id =:userId")
    abstract fun getByCuratorId(userId: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("DELETE FROM `group` WHERE group_id NOT IN(SELECT g.group_id FROM `group` g INNER JOIN group_course gc ON gc.group_id == g.group_id)")
    abstract fun deleteUnrelatedByCourse()
}