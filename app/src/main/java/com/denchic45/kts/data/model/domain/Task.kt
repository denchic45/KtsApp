package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.utils.getExtension
import java.io.File
import java.time.LocalDateTime
import java.util.*


data class Task(
    override var id: String,
    override val courseId: String,
    override val sectionId: String,
    override val name: String,
    override val description: String,
    override val order: Long,
    val completionDate: LocalDateTime?,
    val disabledSendAfterDate: Boolean,
    override val attachments: List<Attachment>,
    val answerType: AnswerType,
    val markType: MarkType,
    override val commentsEnabled: Boolean,
    override val createdDate: Date,
    override val timestamp: Date,
) : CourseContent() {

    private constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0,
        LocalDateTime.now(),
        false,
        emptyList(),
        AnswerType(
            true,
            100,
            false,
            16,
            200
        ),
        MarkType.Score(5),
        false,
        Date(),
        Date()
    )

    companion object {
        fun createEmpty() = Task()
    }
}

data class Attachment(
    val file: File
) : DomainModel() {

    val name: String = file.name

    override var id: String = name

    val extension: String = file.getExtension()
}

data class AnswerType(
    val textAvailable: Boolean,
    val charsLimit: Int,
    val attachmentsAvailable: Boolean,
    val attachmentsLimit: Int,
    val attachmentsSizeLimit: Int
)

sealed class MarkType {
    abstract val position: Int

    data class Score(val maxScore: Int) : MarkType() {
        override val position: Int get() = 0
    }

    object Binary : MarkType() {
        override val position: Int get() = 1
    }
}