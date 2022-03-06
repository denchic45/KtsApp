package com.denchic45.kts.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SubjectDao : BaseDao<SubjectEntity>() {
    @Query("SELECT * FROM subject WHERE subject_id =:subjectId")
    abstract fun getSync(subjectId: String): SubjectEntity?

    @Query("SELECT * FROM subject WHERE subject_id =:subjectId")
    abstract fun get(subjectId: String): LiveData<SubjectEntity>

    @Query("SELECT s.* FROM subject s JOIN course c ON s.subject_id == c.subject_id JOIN group_course gc ON gc.course_id == c.course_id WHERE gc.group_id =:groupId")
    abstract fun getByGroupId(groupId: String): Flow<List<SubjectEntity>>

    @Query("DELETE FROM subject WHERE subject_id NOT IN(SELECT s.subject_id FROM subject s INNER JOIN course c ON c.subject_id == s.subject_id)")
    abstract suspend fun deleteUnrelatedByCourse()
}