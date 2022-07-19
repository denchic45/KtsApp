package com.denchic45.kts.data.local.model

import com.denchic45.kts.CourseEntity
import com.denchic45.kts.SubjectEntity
import com.denchic45.kts.UserEntity

class CourseWithSubjectAndTeacherEntities(
    var courseEntity: CourseEntity,
    var subjectEntity: SubjectEntity,
    var teacherEntity: UserEntity
)