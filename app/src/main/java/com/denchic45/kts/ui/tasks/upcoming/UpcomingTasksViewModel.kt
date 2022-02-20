package com.denchic45.kts.ui.tasks.upcoming

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.domain.usecase.FindTasksForThisGroupThisAndNextWeekUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class UpcomingTasksViewModel @Inject constructor(
    findTasksForThisGroupThisAndNextWeekUseCase: FindTasksForThisGroupThisAndNextWeekUseCase
) : BaseViewModel() {

    val tasks = findTasksForThisGroupThisAndNextWeekUseCase().shareIn(
        viewModelScope,
        SharingStarted.Lazily
    )

    fun onOptionClick(itemId: Int) {
        TODO("Not yet implemented")
    }
}