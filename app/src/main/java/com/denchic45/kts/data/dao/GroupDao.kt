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
    abstract fun getByUuid(id: String): LiveData<GroupWithCuratorAndSpecialtyEntity>

    @Transaction
    @Query("SELECT * FROM `group` WHERE group_id=:uuid")
    abstract fun getByUuidSync(uuid: String?): GroupWithCuratorAndSpecialtyEntity?

    @Query("DELETE FROM `group` WHERE group_id NOT IN(:availableGroups)")
    abstract fun deleteMissing(availableGroups: String?)

    @Query("SELECT group_name FROM `group` WHERE group_id =:groupUuid")
    abstract fun getNameByUuid(groupUuid: String): Flow<String>

    @Query("SELECT timestamp_group FROM `group` WHERE group_id =:uuid")
    abstract fun getTimestampByUuid(uuid: String?): Long

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :uuid)")
    abstract fun isExist(uuid: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE group_id = :uuid)")
    abstract suspend fun isExistSync(uuid: String): Boolean

    @Query("DELETE FROM `group` WHERE group_id =:groupUuid")
    abstract fun deleteByUuid(groupUuid: String?)

    @Query("SELECT * FROM `group` g JOIN user u ON g.group_id = u.uuid_user_group WHERE u.uuid_user =:userUuid ")
    abstract fun getByStudentUuid(userUuid: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("SELECT * FROM `group` WHERE uuid_curator =:userUuid")
    abstract fun getByCuratorUuid(userUuid: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("DELETE FROM `group` WHERE group_id NOT IN(SELECT g.group_id FROM `group` g INNER JOIN group_course gc ON gc.group_id == g.group_id)")
    abstract fun deleteUnrelatedByCourse()
}