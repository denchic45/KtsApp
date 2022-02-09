package com.denchic45.kts.ui.course.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.FindTaskSubmissionsUseCase
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class SubmissionsViewModel @Inject constructor(
    @Named(SubmissionsFragment.TASK_ID) private val taskId: String,
    findTaskSubmissionsUseCase: FindTaskSubmissionsUseCase
) : ViewModel() {

    val openSubmission = SingleLiveData<Pair<String, String>>()

    fun onSubmissionClick(position: Int) {
        viewModelScope.launch {
            openSubmission.value =
                showSubmissions.first()[position].contentId to showSubmissions.first()[position].student.id
        }
    }

    val showSubmissions: SharedFlow<List<Task.Submission>> =
        findTaskSubmissionsUseCase(taskId).shareIn(
            viewModelScope,
            SharingStarted.Lazily, 1
        )


}