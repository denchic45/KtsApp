package com.denchic45.kts.data.local.db

import com.denchic45.kts.*
import com.denchic45.kts.data.local.model.SubmissionWithStudentEntities
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SubmissionLocalDataSource(db: AppDatabase) {

    private val queries: SubmissionEntityQueries = db.submissionEntityQueries

    suspend fun upsert(submissionEntity: SubmissionEntity) = withContext(Dispatchers.IO) {
        queries.upsert(submissionEntity)
    }

    suspend fun upsert(submissionEntities: List<SubmissionEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            submissionEntities.forEach { queries.upsert(it) }
        }
    }

    suspend fun deleteByContentId(id: String) = withContext(Dispatchers.IO) {
        queries.deleteByContentId(id)
    }

    fun getByTaskIdAndUserId(
        contentId: String,
        studentId: String
    ): Flow<SubmissionWithStudentEntities?> {
        return queries.getSubmissionAndStudentByTaskIdAndUserId(
            contentId,
            studentId
        ) { submission_id, student_id, content_id, course_id, status, text, attachments, teacher_id, cause, grade, graded_date, rejectd_date, submitted_date, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_ ->
            SubmissionWithStudentEntities(
                SubmissionEntity(
                    submission_id,
                    student_id,
                    content_id,
                    course_id,
                    status,
                    text,
                    attachments,
                    teacher_id,
                    cause,
                    grade,
                    graded_date,
                    rejectd_date,
                    submitted_date,
                    timestamp
                ),
                UserEntity(
                    user_id,
                    first_name,
                    surname,
                    patronymic,
                    user_group_id,
                    role,
                    email,
                    photo_url,
                    gender,
                    admin,
                    generated_avatar,
                    timestamp
                )
            )
        }.asFlow().mapToOne(Dispatchers.IO)
    }

    fun getByTaskId(taskId: String): Flow<List<SubmissionWithStudentEntities>> {
        return queries.getSubmissionsAndStudentsByTaskId(taskId) { submission_id, student_id, content_id, course_id, status, text, attachments, teacher_id, cause, grade, graded_date, rejectd_date, submitted_date, timestamp, user_id, first_name, surname, patronymic, user_group_id, role, email, photo_url, gender, admin, generated_avatar, timestamp_ ->
            SubmissionWithStudentEntities(
                SubmissionEntity(
                    submission_id,
                    student_id,
                    content_id,
                    course_id,
                    status,
                    text,
                    attachments,
                    teacher_id,
                    cause,
                    grade,
                    graded_date,
                    rejectd_date,
                    submitted_date,
                    timestamp
                ),
                UserEntity(
                    user_id,
                    first_name,
                    surname,
                    patronymic,
                    user_group_id,
                    role,
                    email,
                    photo_url,
                    gender,
                    admin,
                    generated_avatar,
                    timestamp
                )
            )
        }
            .asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getStudentsWithoutSubmission(taskId: String): List<UserEntity> =
        withContext(Dispatchers.IO) {
            queries.getStudentsWithoutSubmission(taskId).executeAsList()
        }
}