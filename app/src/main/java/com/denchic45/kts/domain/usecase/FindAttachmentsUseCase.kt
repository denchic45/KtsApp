package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Attachment
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindAttachmentsUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    operator fun invoke(contentId: String): Flow<List<Attachment>> =
        courseRepository.findAttachmentsByContentId(contentId)
}