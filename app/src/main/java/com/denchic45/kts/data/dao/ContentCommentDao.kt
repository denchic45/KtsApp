package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.ContentCommentEntity

@Dao
abstract class ContentCommentDao : BaseDao<ContentCommentEntity>() {

    @Query("DELETE FROM content_comment WHERE content_id=:id")
    abstract fun deleteByContentId(id: String)
}
