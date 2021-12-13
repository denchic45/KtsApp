package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao : BaseDao<TaskEntity>() {

    @Query("SELECT * FROM task WHERE courseUuid =:courseUuid")
    abstract fun getByCourseUuid(courseUuid: String) :Flow<List<TaskEntity>>
}