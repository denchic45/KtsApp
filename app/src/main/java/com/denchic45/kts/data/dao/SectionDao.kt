package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SectionDao: BaseDao<SectionEntity>() {

    @Query("SELECT * FROM section WHERE courseUuid =:uuid")
    abstract fun getByCourseUuid(uuid:String): Flow<List<SectionEntity>>
}