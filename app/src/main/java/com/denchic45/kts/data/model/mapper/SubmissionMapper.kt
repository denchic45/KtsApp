package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.firestore.SubmissionDoc
import com.denchic45.kts.data.model.room.SubmissionCommentEntity
import com.denchic45.kts.data.model.room.SubmissionEntity
import com.denchic45.kts.data.model.room.SubmissionWithStudentUserAndCommentsEntities
import com.denchic45.kts.util.toLocalDateTime
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.LocalDateTime
import java.time.ZoneId

@Mapper(uses = [UserMapper::class])
abstract class SubmissionMapper {
    val userMapper = UserMapperImpl()

    @Mapping(target = "id", ignore = true)
    abstract fun docToEntity(submissionDoc: SubmissionDoc): SubmissionEntity

    fun entityToDomain(
        entities: SubmissionWithStudentUserAndCommentsEntities,
        attachments: List<Attachment>
    ): Task.Submission {
        val content = Task.Submission.Content(entities.submissionEntity.text, attachments)
        val submissionStatus = getSubmissionStatus(entities)
        val comments = getSubmissionComments(entities.submissionCommentEntities)
        return Task.Submission(
            entities.submissionEntity.contentId,
            userMapper.entityToDomain(entities.studentEntity),
            content,
            comments,
            submissionStatus,
            entities.submissionEntity.contentUpdateDate!!.toLocalDateTime()
        )
    }

    abstract fun getSubmissionComments(commentEntities: List<SubmissionCommentEntity>): List<Task.Comment>

    private fun getSubmissionStatus(entities: SubmissionWithStudentUserAndCommentsEntities): Task.SubmissionStatus {

        return when (entities.submissionEntity.status) {
            Task.Submission.Status.NOT_SUBMITTED -> Task.SubmissionStatus.NotSubmitted
            Task.Submission.Status.SUBMITTED -> Task.SubmissionStatus.Submitted(
                LocalDateTime.ofInstant(
                    entities.submissionEntity.submittedDate!!.toInstant(),
                    ZoneId.systemDefault()
                )
            )
            Task.Submission.Status.GRADED -> {
                Task.SubmissionStatus.Graded(
                    userMapper.entityToDomain(entities.teacherEntity!!),
                    entities.submissionEntity.grade!!,
                    entities.submissionEntity.gradedDate!!.toLocalDateTime()
                )
            }
            Task.Submission.Status.REJECTED -> Task.SubmissionStatus.Rejected(
                userMapper.entityToDomain(entities.teacherEntity!!),
                entities.submissionEntity.cause,
                entities.submissionEntity.rejectedDate!!.toLocalDateTime()
            )
        }
    }

    fun domainToStatus(submission: Task.Submission): Task.Submission.Status {
        return when (submission.status) {
            is Task.SubmissionStatus.NotSubmitted -> Task.Submission.Status.NOT_SUBMITTED
            is Task.SubmissionStatus.Submitted -> Task.Submission.Status.SUBMITTED
            is Task.SubmissionStatus.Graded -> Task.Submission.Status.GRADED
            is Task.SubmissionStatus.Rejected -> Task.Submission.Status.REJECTED

        }
    }

//    @Mapping(source = "entity.userEntity", target = "student")
//    @Mapping(source = "entity.submissionEntity", target = ".")
//    abstract fun entityToDraftSubmissionDomain(
//        entity: SubmissionWithStudentUserCommentsEntities,
//        content: Task.Submission.Content
//    ): Task.SubmissionStatus.Draft
//
//    @Mapping(source = "entity.submissionEntity", target = ".")
//    abstract fun entityToDoneSubmissionDomain(
//        entity: SubmissionWithStudentUserCommentsEntities,
//        content: Task.Submission.Content
//    ): Task.SubmissionStatus.Done
//
//    @Mapping(source = "entity.submissionEntity", target = ".")
//    abstract fun entityToGradedSubmissionDomain(
//        entity: SubmissionWithStudentUserCommentsEntities,
//        content: Task.Submission.Content
//    ): Task.SubmissionStatus.Graded

}