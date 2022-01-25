package com.denchic45.kts.data.dao

import androidx.room.Dao
import com.denchic45.kts.data.model.room.SubmissionCommentEntity

@Dao
abstract class SubmissionCommentDao : BaseDao<SubmissionCommentEntity>()