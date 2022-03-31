package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.SpecialtyRepository
import javax.inject.Inject

class FindSpecialtyByContainsNameUseCase @Inject constructor(
    specialtyRepository: SpecialtyRepository
) : FindByContainsNameUseCase<Specialty>(specialtyRepository)