package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import javax.inject.Inject

class FindSubjectByContainsNameUseCase @Inject constructor(
    subjectRepository: SubjectRepository
) : FindByContainsNameUseCase<Subject>(subjectRepository)