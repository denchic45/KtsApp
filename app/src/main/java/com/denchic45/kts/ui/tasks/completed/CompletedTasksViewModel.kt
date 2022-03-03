package com.denchic45.kts.ui.tasks.completed

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.domain.usecase.FindCompletedTasksForYourGroupUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class CompletedTasksViewModel @Inject constructor(
    findCompletedTasksForYourGroupUseCase: FindCompletedTasksForYourGroupUseCase
) : BaseViewModel() {
    val tasks = findCompletedTasksForYourGroupUseCase().shareIn(
        viewModelScope,
        SharingStarted.Lazily
    )
}