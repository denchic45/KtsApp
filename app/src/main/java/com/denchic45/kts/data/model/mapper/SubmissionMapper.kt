package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.firestore.SubmissionDoc
import com.denchic45.kts.data.model.room.SubmissionCommentEntity
import com.denchic45.kts.data.model.room.SubmissionEntity
import com.denchic45.kts.data.model.room.SubmissionWithStudentUserCommentsEntities
import org.mapstruct.Mapper
import java.time.LocalDateTime
import java.time.ZoneId

@Mapper(uses = [UserMapper::class])
abstract class SubmissionMapper {
    val userMapper = UserMapperImpl()
    abstract fun docToEntity(submissionDoc: SubmissionDoc): SubmissionEntity

    fun entityToDomain(
        entities: SubmissionWithStudentUserCommentsEntities,
        attachments: List<Attachment>
    ): Task.Submission {
        val content = Task.Submission.Content(entities.submissionEntity.text, attachments)
        val submissionStatus = getSubmissionStatus(entities)
        val comments = getSubmissionComments(entities.submissionCommentEntities)
        return Task.Submission(userMapper.entityToDomain(entities.studentEntity), content, comments, submissionStatus)
    }

    abstract fun getSubmissionComments(commentEntities: List<SubmissionCommentEntity>): List<Task.Comment>

    fun getSubmissionStatus(entities: SubmissionWithStudentUserCommentsEntities): Task.SubmissionStatus {

        return when (entities.submissionEntity.status) {
            Task.Submission.Status.NOTHING -> Task.SubmissionStatus.Nothing
            Task.Submission.Status.DRAFT -> { Task.SubmissionStatus.Draft }
            Task.Submission.Status.DONE -> Task.SubmissionStatus.Done( LocalDateTime.ofInstant(entities.submissionEntity.doneDate.toInstant(), ZoneId.systemDefault()) )
            Task.Submission.Status.GRADED -> {
                Task.SubmissionStatus.Graded(userMapper.entityToDomain(entities.teacherEntity), LocalDateTime.ofInstant(entities.submissionEntity.gradedDate.toInstant(),ZoneId.systemDefault()))
            }
            Task.Submission.Status.REJECTED -> Task.SubmissionStatus.Rejected(userMapper.entityToDomain(entities.teacherEntity), entities.submissionEntity.cause)
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