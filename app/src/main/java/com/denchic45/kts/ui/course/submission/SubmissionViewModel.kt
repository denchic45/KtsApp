package com.denchic45.kts.ui.course.submission

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.FindTaskSubmissionUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.course.content.ContentFragment
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Named

class SubmissionViewModel @Inject constructor(
    @Named(SubmissionFragment.TASK_ID) taskId: String,
    @Named(SubmissionFragment.STUDENT_ID) studentId: String,
    findTaskSubmissionUseCase: FindTaskSubmissionUseCase
) : BaseViewModel() {
    val showSubmission: SharedFlow<Task.Submission> = findTaskSubmissionUseCase(taskId, studentId)
        .shareIn(
            viewModelScope,
            SharingStarted.Lazily, 1
        )
}