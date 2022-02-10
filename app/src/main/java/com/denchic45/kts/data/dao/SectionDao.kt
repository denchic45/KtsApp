package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SectionDao : BaseDao<SectionEntity>() {

    @Query("SELECT * FROM section WHERE course_id =:courseId ORDER BY `order`")
    abstract fun getByCourseId(courseId: String): Flow<List<SectionEntity>>

    @Query("SELECT * FROM section WHERE section_id=:sectionId")
    abstract suspend fun get(sectionId: String): SectionEntity?

    @Query("DELETE FROM section WHERE section_id NOT IN(SELECT section_id FROM section WHERE section_id IN(:availableSections))")
    abstract fun deleteMissing(availableSections: List<String>)
}