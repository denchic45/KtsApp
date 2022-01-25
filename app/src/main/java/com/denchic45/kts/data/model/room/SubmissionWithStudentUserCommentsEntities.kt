package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Relation

data class SubmissionWithStudentUserCommentsEntities(
    @Embedded
    val submissionEntity: SubmissionEntity,
    @Relation(parentColumn = "submission_id", entityColumn = "submission_id")
    val submissionCommentEntity: SubmissionCommentEntity,
    @Relation(parentColumn = "student_id", entityColumn = "user_id")
    val userEntity: UserEntity
)
