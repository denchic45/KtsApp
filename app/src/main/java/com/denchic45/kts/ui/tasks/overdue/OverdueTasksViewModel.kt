package com.denchic45.kts.ui.tasks.overdue

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.domain.usecase.FindOverdueTasksForYourGroupUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class OverdueTasksViewModel @Inject constructor(
    findOverdueTasksForYourGroupUseCase: FindOverdueTasksForYourGroupUseCase
) : BaseViewModel() {

    val tasks = findOverdueTasksForYourGroupUseCase().shareIn(
        viewModelScope,
        SharingStarted.Lazily
    )

}