package com.denchic45.kts.data.local.model

import com.denchic45.kts.SubmissionEntity
import com.denchic45.kts.UserEntity

class SubmissionWithStudentEntities(
    val submissionEntity: SubmissionEntity,
    val studentEntity: UserEntity
) {
}