package com.denchic45.kts.ui.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.SubmissionSettings
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.FindAttachmentsUseCase
import com.denchic45.kts.domain.usecase.FindSelfTaskSubmissionUseCase
import com.denchic45.kts.domain.usecase.FindTaskUseCase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
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
    findTaskAttachmentsUseCase: FindAttachmentsUseCase,
    findSelfTaskSubmissionUseCase: FindSelfTaskSubmissionUseCase
) : ViewModel() {

    private val taskFlow = findTaskUseCase(taskId).shareIn(
        viewModelScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )
    val taskAttachments = findTaskAttachmentsUseCase(taskId)
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
            submissionSettings = task.submissionSettings
        )
    }


    val showSubmissionToolbar = MutableLiveData<Boolean>()
    val expandBottomSheet = MutableLiveData(BottomSheetBehavior.STATE_COLLAPSED)

    private var oldContent = Task.Submission.Content("", emptyList())

    private var content = oldContent

    private val _submissionViewState = MutableSharedFlow<Task.Submission>(replay = 1)
    val submissionViewState = _submissionViewState.asSharedFlow()

    init {
        viewModelScope.launch {
            _submissionViewState.emitAll(findSelfTaskSubmissionUseCase(taskId))
        }
    }


    val openFilePicker = SingleLiveData<Unit>()
    val openAttachment = SingleLiveData<File>()

    fun onBottomSheetStateChanged(newState: Int) {
        expandBottomSheet.value = newState

        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            showSubmissionToolbar.value = false
        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            showSubmissionToolbar.value = true
        }
    }

    fun onSubmissionAttachmentClick(position: Int) {
        viewModelScope.launch { openAttachment.value = submissionViewState.first().content.attachments[position].file }
    }

    fun onAddAttachmentClick() {
        openFilePicker.call()
    }

    fun onSelectedFile(file: File) {
        viewModelScope.launch {
            content = content.copy(attachments = content.attachments + Attachment(file))
            _submissionViewState.emit(submissionViewState.first().copy(content = content))
        }
    }

    fun onTaskFileClick(position: Int) {
        viewModelScope.launch {
            openAttachment.value = taskFlow.first().attachments[position].file
        }
    }

    data class TaskViewState(
        val name: String,
        val description: String,
        val dateWithTimeLeft: Pair<String, String>?,
        val submissionSettings: SubmissionSettings
    )

}