package com.denchic45.kts.data.model.room

import com.denchic45.kts.data.model.EntityModel
import androidx.room.Embedded
import com.denchic45.kts.data.model.room.CourseEntity
import com.denchic45.kts.data.model.room.GroupEntity
import androidx.room.Junction
import androidx.room.Relation
import com.denchic45.kts.data.model.room.GroupCourseCrossRef
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import com.denchic45.kts.data.model.room.SubjectEntity
import com.denchic45.kts.data.model.room.UserEntity

class CourseWithSubjectWithTeacherAndGroups : EntityModel {
    @Embedded
    var courseEntity: CourseEntity? = null

    @Relation(
        entity = GroupEntity::class,
        parentColumn = "uuid_course",
        entityColumn = "group_id",
        associateBy = Junction(
            GroupCourseCrossRef::class
        )
    )
    var groupEntities: List<GroupWithCuratorAndSpecialtyEntity>? = null

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity? = null

    @Relation(parentColumn = "uuid_teacher", entityColumn = "uuid_user")
    var teacherEntity: UserEntity? = null
}

class CourseWithSubjectAndTeacher : EntityModel {
    @Embedded
    var courseEntity: CourseEntity? = null

    @Relation(
        entity = GroupEntity::class,
        parentColumn = "uuid_course",
        entityColumn = "group_id",
        associateBy = Junction(
            GroupCourseCrossRef::class
        )
    )
    var groupEntities: List<GroupWithCuratorAndSpecialtyEntity>? = null

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity? = null

    @Relation(parentColumn = "uuid_teacher", entityColumn = "uuid_user")
    var teacherEntity: UserEntity? = null
}