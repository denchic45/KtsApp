package com.denchic45.kts.data.dao

import androidx.room.Dao
import com.denchic45.kts.data.dao.BaseDao
import com.denchic45.kts.data.model.room.GroupEntity
import androidx.lifecycle.LiveData
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {
    @get:Query("SELECT * FROM `group`")
    abstract val allGroups: LiveData<List<GroupEntity?>?>?

    @Transaction
    @Query("SELECT * FROM `group` WHERE uuid_group=:uuid")
    abstract fun getByUuid(uuid: String): LiveData<GroupWithCuratorAndSpecialtyEntity>

    @Transaction
    @Query("SELECT * FROM `group` WHERE uuid_group=:uuid")
    abstract fun getByUuidSync(uuid: String?): GroupWithCuratorAndSpecialtyEntity?

    @Query("DELETE FROM `group` WHERE uuid_group NOT IN(:availableGroups)")
    abstract fun deleteMissing(availableGroups: String?)

    @Query("SELECT name_group FROM `group` WHERE uuid_group =:groupUuid")
    abstract fun getNameByUuid(groupUuid: String): Flow<String>

    @Query("SELECT timestamp_group FROM `group` WHERE uuid_group =:uuid")
    abstract fun getTimestampByUuid(uuid: String?): Long

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE uuid_group = :uuid)")
    abstract fun isExist(uuid: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM `group` WHERE uuid_group = :uuid)")
    abstract suspend fun isExistSync(uuid: String?): Boolean

    @Query("DELETE FROM `group` WHERE uuid_group =:groupUuid")
    abstract fun deleteByUuid(groupUuid: String?)

    @Query("SELECT * FROM `group` g JOIN user u ON g.uuid_group = u.uuid_user_group WHERE u.uuid_user =:userUuid ")
    abstract fun getByStudentUuid(userUuid: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("SELECT * FROM `group` WHERE uuid_curator =:userUuid")
    abstract fun getByCuratorUuid(userUuid: String): Observable<GroupWithCuratorAndSpecialtyEntity>

    @Query("DELETE FROM `group` WHERE uuid_group NOT IN(SELECT g.uuid_group FROM `group` g INNER JOIN group_course gc ON gc.uuid_group == g.uuid_group)")
    abstract fun deleteUnrelatedByCourse()
}