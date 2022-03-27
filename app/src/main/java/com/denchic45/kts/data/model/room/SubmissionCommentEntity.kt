package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.EntityModel
import com.google.firebase.firestore.Exclude
import java.util.*

@Entity(
    tableName = "submission_comment",
    foreignKeys = [
        ForeignKey(
            entity = SubmissionEntity::class,
            parentColumns = ["submission_id"],
            childColumns = ["submission_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class SubmissionCommentEntity(
    @PrimaryKey(autoGenerate = true)
    @Exclude
    val id: Int,
    @ColumnInfo(name = "submission_id")
    val submissionId: String,
    val content: String,
    val authorId: String,
    @field:TypeConverters(DateConverter::class)
    val createdDate: Date
) : EntityModel, DocModel