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
    val submissionSettings: SubmissionSettings,
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
        SubmissionSettings(
            true,
            100,
            false,
            16,
            200
        ),
        false,
        Date(),
        Date()
    )

    companion object {
        fun createEmpty() = Task()
    }

    data class Submission(
        val student: User,
        val content: Content,
        val comments: List<Comment>,
        val status: SubmissionStatus
    ) {
        data class Content(
            val text: String,
            val attachments: List<Attachment>
        )

        enum class Status { NOTHING, DRAFT, DONE, GRADED, REJECTED }
    }

    sealed class SubmissionStatus {

        object Nothing: SubmissionStatus()

        object Draft: SubmissionStatus()

        data class Done(
            val doneDate: LocalDateTime
        ) : SubmissionStatus()

        data class Graded(
            val teacher: User,
            val gradeDate: LocalDateTime
        ) : SubmissionStatus()

        data class Rejected(
            val teacher: User,
            val cause: String
        ) : SubmissionStatus()
    }

    data class Comment(
        override var id: String,
        val content: String,
        val author: User,
        val createdDate: LocalDateTime
    ) : DomainModel()
}

data class Attachment(
    val file: File
) : DomainModel() {

    val name: String = file.name

    override var id: String = name

    val extension: String = file.getExtension()
}

data class SubmissionSettings(
    val textAvailable: Boolean,
    val charsLimit: Int,
    val attachmentsAvailable: Boolean,
    val attachmentsLimit: Int,
    val attachmentsSizeLimit: Int
)

//sealed class GradeType {
//    abstract val position: Int
//
//    data class Score(val maxScore: Int) : GradeType() {
//        override val position: Int get() = 0
//    }
//
//    object Binary : GradeType() {
//        override val position: Int get() = 1
//    }
//}