package com.denchic45.kts.domain.model

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.util.Files
import com.denchic45.kts.util.getExtension
import com.denchic45.kts.util.toString
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

    val weekDate: String?
        get() = completionDate?.toString("w_y")

    private constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0,
        LocalDateTime.MIN,
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
        Date(0),
        Date(0)
    )

    companion object {
        fun createEmpty() = Task()
    }

    data class Submission(
        val contentId: String,
        val student: User,
        val content: Content,
        val status: SubmissionStatus,
        val contentUpdateDate: LocalDateTime
    ) : DomainModel {

        override var id: String = ""

        val submitted: Boolean
            get() = status !is SubmissionStatus.NotSubmitted

        companion object {
            fun createEmpty(contentId: String, student: User): Submission {
                return Submission(
                    contentId,
                    student,
                    Content.createEmpty(),
                    SubmissionStatus.NotSubmitted,
                    LocalDateTime.now()
                )
            }
        }

        data class Content(
            val text: String,
            val attachments: List<Attachment>
        ) {
            companion object {
                fun createEmpty(): Content = Content("", emptyList())
            }

            fun isEmpty(): Boolean = text.isEmpty() && attachments.isEmpty()

            fun isNotEmpty(): Boolean = text.isNotEmpty() || attachments.isNotEmpty()

            fun hasText(): Boolean = text.isNotEmpty()

            fun hasAttachments(): Boolean = attachments.isNotEmpty()

            fun hasAll(): Boolean = text.isNotEmpty() && attachments.isNotEmpty()
        }

        enum class Status { NOT_SUBMITTED, SUBMITTED, GRADED, REJECTED }
    }

    sealed class SubmissionStatus {

        object NotSubmitted : SubmissionStatus()

        data class Submitted(
            val submittedDate: LocalDateTime = LocalDateTime.now()
        ) : SubmissionStatus()

        data class Graded(
            val teacher: User,
            val grade: Int,
            val gradedDate: LocalDateTime
        ) : SubmissionStatus()

        data class Rejected(
            val teacher: User,
            val cause: String,
            val rejectedDate: LocalDateTime
        ) : SubmissionStatus()
    }

    data class Comment(
        override val id: String,
        val content: String,
        val author: User,
        val createdDate: LocalDateTime
    ) : DomainModel
}

data class Attachment(
    val file: File
) : DomainModel {

    val name: String = Files.nameWithoutTimestamp(file.name)

    override var id: String = name

    val extension: String = file.getExtension()
}

data class SubmissionSettings(
    val textAvailable: Boolean,
    val charsLimit: Int,
    val attachmentsAvailable: Boolean,
    val attachmentsLimit: Int,
    val attachmentsSizeLimit: Int
) {

    private constructor() : this(false, 100, true, 16, 200)

    fun onlyTextAvailable(): Boolean = textAvailable && !attachmentsAvailable

    fun onlyAttachmentsAvailable(): Boolean = !textAvailable && attachmentsAvailable

    fun allAvailable(): Boolean = textAvailable && attachmentsAvailable
}