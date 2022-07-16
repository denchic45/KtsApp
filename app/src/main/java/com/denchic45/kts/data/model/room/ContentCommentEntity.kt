package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.domain.EntityModel
import com.google.firebase.firestore.Exclude
import java.util.*

@Entity(
    tableName = "content_comment",
    foreignKeys = [
        ForeignKey(
            entity = CourseContentEntity::class,
            parentColumns = ["content_id"],
            childColumns = ["content_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ContentCommentEntity(
    @PrimaryKey(autoGenerate = true)
    @Exclude
    val id: Int,
    @ColumnInfo(name = "content_id")
    val contentId: String,
    val content: String,
    val authorId: String,
    @field:TypeConverters(DateConverter::class)
    val createdDate: Date
) : EntityModel, DocModel
