package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.firestore.SubmissionDoc
import com.denchic45.kts.data.model.room.SubmissionEntity
import com.denchic45.kts.data.model.room.SubmissionWithStudentUserCommentsEntities
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(uses = [UserMapper::class])
abstract class SubmissionMapper {

    abstract fun docToEntity(submissionDoc: SubmissionDoc): SubmissionEntity

    fun entityToDomain(
        submissionWithStudentUserCommentsEntities: SubmissionWithStudentUserCommentsEntities,
        attachments: List<Attachment>
    ): Task.Submission {
        val content = Task.Submission.Content(
            submissionWithStudentUserCommentsEntities.submissionEntity.text,
            attachments
        )
        return when (submissionWithStudentUserCommentsEntities.submissionEntity.status) {
            Task.Submission.Status.NOTHING -> throw IllegalStateException()
            Task.Submission.Status.DRAFT -> {

                entityToDraftSubmissionDomain(
                    submissionWithStudentUserCommentsEntities, content
                )
            }
            Task.Submission.Status.DONE -> entityToDraftSubmissionDomain(
                submissionWithStudentUserCommentsEntities, content
            )
            Task.Submission.Status.GRADED -> entityToGradedSubmissionDomain(
                submissionWithStudentUserCommentsEntities, content
            )
            Task.Submission.Status.REJECTED -> TODO()
        }
    }

    @Mapping(source = "entity.userEntity", target = "student")
    @Mapping(source = "entity.submissionEntity", target = ".")
    abstract fun entityToDraftSubmissionDomain(
        entity: SubmissionWithStudentUserCommentsEntities,
        content: Task.Submission.Content
    ): Task.Submission.Draft

    @Mapping(source = "entity.submissionEntity", target = ".")
    abstract fun entityToDoneSubmissionDomain(
        entity: SubmissionWithStudentUserCommentsEntities,
        content: Task.Submission.Content
    ): Task.Submission.Done

    @Mapping(source = "entity.submissionEntity", target = ".")
    abstract fun entityToGradedSubmissionDomain(
        entity: SubmissionWithStudentUserCommentsEntities,
        content: Task.Submission.Content
    ): Task.Submission.Graded

}