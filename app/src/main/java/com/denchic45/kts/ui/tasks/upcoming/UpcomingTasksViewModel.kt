package com.denchic45.kts.ui.tasks.upcoming

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.domain.usecase.FindUpcomingTasksForYourGroupUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class UpcomingTasksViewModel @Inject constructor(
    findUpcomingTasksForYourGroupUseCase: FindUpcomingTasksForYourGroupUseCase
) : BaseViewModel() {

    val tasks = findUpcomingTasksForYourGroupUseCase().shareIn(
        viewModelScope,
        SharingStarted.Lazily
    )
}