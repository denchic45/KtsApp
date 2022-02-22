package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SubmissionEntity
import com.denchic45.kts.data.model.room.SubmissionWithStudentUserAndCommentsEntities
import com.denchic45.kts.data.model.room.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SubmissionDao : BaseDao<SubmissionEntity>() {

    @Query("DELETE FROM submission WHERE content_id=:id")
    abstract fun deleteByContentId(id: String)

    @Query("SELECT * FROM submission WHERE content_id=:contentId AND student_id=:studentId")
    abstract fun getByTaskIdAndUserId(
        contentId: String,
        studentId: String
    ): Flow<SubmissionWithStudentUserAndCommentsEntities?>

    @Query("SELECT * FROM submission WHERE content_id=:contentId AND student_id=:studentId")
    abstract suspend fun getByContentIdAndUserIdSync(
        contentId: String,
        studentId: String
    ): SubmissionWithStudentUserAndCommentsEntities?

    @Query("SELECT * FROM submission WHERE content_id=:taskId")
    abstract fun getByTaskId(taskId: String): Flow<List<SubmissionWithStudentUserAndCommentsEntities>>


    @Query(
        """
SELECT 
  * 
FROM 
  user u 
  LEFT JOIN submission s ON s.student_id = u.user_id 
WHERE 
  u.user_group_id IN(
    SELECT 
      u.user_group_id 
    FROM 
      user u 
      JOIN group_course gc ON u.user_group_id = gc.group_id 
      JOIN course_content cc ON gc.course_id = cc.course_id 
    WHERE 
      cc.content_id =:taskId
  ) 
  AND s.student_id IS NULL
    """
    )
    abstract suspend fun getStudentsWithoutSubmission(taskId: String): List<UserEntity>

}