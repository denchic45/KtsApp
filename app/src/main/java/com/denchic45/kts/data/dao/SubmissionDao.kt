package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SubmissionEntity
import com.denchic45.kts.data.model.room.SubmissionWithStudentUserCommentsEntities
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SubmissionDao : BaseDao<SubmissionEntity>() {

    @Query("DELETE FROM submission WHERE content_id=:id")
    abstract fun deleteByContentId(id: String)

    @Query("SELECT * FROM submission WHERE content_id=:contentId AND student_id=:studentId")
    abstract fun getByTaskIdAndUserId(
        contentId: String,
        studentId: String
    ): Flow<SubmissionWithStudentUserCommentsEntities?>

    @Query("SELECT * FROM submission WHERE content_id=:contentId AND student_id=:studentId")
    abstract suspend fun getByContentIdAndUserIdSync(
        contentId: String,
        studentId: String
    ): SubmissionWithStudentUserCommentsEntities?
}