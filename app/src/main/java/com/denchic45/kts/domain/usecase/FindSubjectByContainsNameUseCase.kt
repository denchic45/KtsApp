package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.GroupHeader
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FindSubjectByContainsNameUseCase @Inject constructor(
    subjectRepository: SubjectRepository
) : FindByContainsNameUseCase<Subject>(subjectRepository)