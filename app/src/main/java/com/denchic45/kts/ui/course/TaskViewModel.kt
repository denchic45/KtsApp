package com.denchic45.kts.ui.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.domain.usecase.FindSelfTaskSubmissionUseCase
import com.denchic45.kts.domain.usecase.FindTaskUseCase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TaskViewModel @Inject constructor(
    @Named(TaskFragment.TASK_ID) val taskId: String,
    findTaskUseCase: FindTaskUseCase,
    findSelfTaskSubmissionUseCase: FindSelfTaskSubmissionUseCase
) : ViewModel() {

    private val taskFlow = findTaskUseCase(taskId).shareIn(
        viewModelScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )
    val taskViewState = taskFlow.map { task ->
        TaskViewState(
            name = task.name,
            description = task.description,
            dateWithTimeLeft = task.completionDate?.let {
                val pattern = DateTimeFormatter.ofPattern("dd MMM HH:mm")

                task.completionDate.format(pattern) to
                        "Осталось ${
                            DateTimeFormatter.ofPattern("d дней H час. m мин.").format(
                                Instant.ofEpochMilli(
                                    Duration.between(
                                        task.completionDate,
                                        LocalDateTime.now()
                                    ).toMillis()
                                ).atZone(
                                    ZoneId.systemDefault()
                                ).toLocalDateTime()
                            )
                        }"
            },
            attachments = emptyList()
        )
    }

    val showSubmissionToolbar = MutableLiveData<Boolean>()

    val expandBottomSheet = MutableLiveData(BottomSheetBehavior.STATE_COLLAPSED)

    val submissionViewState = findSelfTaskSubmissionUseCase(taskId)

    fun onBottomSheetStateChanged(newState: Int) {
        expandBottomSheet.value = newState

        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            showSubmissionToolbar.value = false
        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            showSubmissionToolbar.value = true
        }
    }

    data class TaskViewState(
        val name: String,
        val description: String,
        val dateWithTimeLeft: Pair<String, String>?,
        val attachments: List<Attachment>
    )

}