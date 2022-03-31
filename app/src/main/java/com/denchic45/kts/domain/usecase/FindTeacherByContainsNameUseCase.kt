package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import javax.inject.Inject

class FindTeacherByContainsNameUseCase @Inject constructor(
    teacherRepository: TeacherRepository
) : FindByContainsNameUseCase<User>(teacherRepository)