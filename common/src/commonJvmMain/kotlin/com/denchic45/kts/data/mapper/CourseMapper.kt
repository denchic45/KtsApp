package com.denchic45.kts.data.mapper

import com.denchic45.kts.CourseEntity
import com.denchic45.kts.GetCourseWithSubjectAndTeacherByTeacherId
import com.denchic45.kts.GetCourseWithSubjectWithTeacherAndGroupsById
import com.denchic45.kts.data.local.model.CourseWithSubjectAndTeacherEntities
import com.denchic45.kts.data.remote.model.CourseDoc
import com.denchic45.kts.domain.model.*
import java.util.*

@Deprecated("")
fun CourseDoc.toEntity() = CourseEntity(
    course_id = id,
    name = name,
    subject_id = subject.id,
    teacher_id = teacher.id
)

@Deprecated("")
fun CourseDoc.toCourseHeader() = CourseHeader(
    id = id,
    name = name,
    subject = subject.toDomain(),
    teacher = teacher.toDomain()
)

fun List<CourseDoc>.docsToCourseHeaders() = map { it.toCourseHeader() }

fun List<GetCourseWithSubjectWithTeacherAndGroupsById>.toDomain() = Course(
    id = first().course_id,
    name = first().name,
    subject = first().run {
        Subject(
            id = subject_id,
            name = name,
            iconUrl = icon_url,
            colorName = color_name
        )
    },
    teacher = first().run {
        User(
            id = teacher_id,
            firstName = first_name,
            surname = surname,
            patronymic = patronymic,
            groupId = user_group_id,
            role = User.Role.valueOf(role),
            email = email,
            photoUrl = photo_url,
            timestamp = Date(timestamp),
            gender = gender,
            generatedAvatar = generated_avatar,
            admin = admin
        )
    },
    groupHeaders = map {
        GroupHeader(
            id = it.group_id,
            name = it.name_,
            specialtyId = it.specialty_id
        )
    }
)

fun CourseWithSubjectAndTeacherEntities.toDomain() = CourseHeader(
    id = courseEntity.course_id,
    name = courseEntity.name,
    subject = subjectEntity.toDomain(),
    teacher = teacherEntity.toDomain()
)

fun List<CourseWithSubjectAndTeacherEntities>.entitiesToCourseHeaders() = map { it.toDomain() }

fun GetCourseWithSubjectAndTeacherByTeacherId.toDomain() = CourseHeader(
    id = course_id,
    name = name,
    subject = Subject(
        id = subject_id,
        name = name,
        iconUrl = icon_url,
        colorName = color_name
    ),
    User(
        id = teacher_id,
        firstName = first_name,
        surname = surname,
        patronymic = patronymic,
        groupId = user_group_id,
        role = User.Role.valueOf(role),
        email = email,
        photoUrl = photo_url,
        timestamp = Date(timestamp),
        gender = gender,
        generatedAvatar = generated_avatar,
        admin = admin
    )
)

fun List<GetCourseWithSubjectAndTeacherByTeacherId>.entitiesToDomains() = map { it.toDomain() }