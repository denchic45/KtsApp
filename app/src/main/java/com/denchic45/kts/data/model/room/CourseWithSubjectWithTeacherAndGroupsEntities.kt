package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.denchic45.kts.domain.EntityModel

data class CourseWithSubjectWithTeacherAndGroupsEntities(
    @Embedded
    var courseEntity: CourseEntity,

    @Relation(
        entity = GroupEntity::class,
        parentColumn = "course_id",
        entityColumn = "group_id",
        associateBy = Junction(
            GroupCourseCrossRef::class
        )
    )
    var groupEntities: List<GroupWithCuratorAndSpecialtyEntity>,

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity,

    @Relation(parentColumn = "teacher_id", entityColumn = "user_id")
    var teacherEntity: UserEntity
) : EntityModel

data class CourseWithSubjectAndTeacherEntities(
    @Embedded
    var courseEntity: CourseEntity,

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity,

    @Relation(parentColumn = "teacher_id", entityColumn = "user_id")
    var teacherEntity: UserEntity
) : EntityModel