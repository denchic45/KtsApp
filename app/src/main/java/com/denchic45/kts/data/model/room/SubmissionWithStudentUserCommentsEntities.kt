package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Relation

data class SubmissionWithStudentUserCommentsEntities(
    @Embedded
    val submissionEntity: SubmissionEntity,
    @Relation(parentColumn = "submission_id", entityColumn = "submission_id")
    val submissionCommentEntities: List<SubmissionCommentEntity>,
    @Relation(parentColumn = "student_id", entityColumn = "user_id")
    val studentEntity: UserEntity,
    @Relation(parentColumn = "grading_teacher_id", entityColumn = "user_id")
    val teacherEntity: UserEntity?
)
