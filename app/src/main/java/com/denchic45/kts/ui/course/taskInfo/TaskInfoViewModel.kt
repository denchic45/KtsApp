package com.denchic45.kts.ui.course.taskInfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.SubmissionSettings
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.course.taskEditor.AddAttachmentItem
import com.denchic45.kts.utils.toString
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

class TaskInfoViewModel @Inject constructor(
    @Named(TaskInfoFragment.TASK_ID) val taskId: String,
    @Named(TaskInfoFragment.COURSE_ID) val courseId: String,
    findSelfUserUseCase: FindSelfUserUseCase,
    findTaskUseCase: FindTaskUseCase,
    findTaskAttachmentsUseCase: FindAttachmentsUseCase,
    findSelfTaskSubmissionUseCase: FindSelfTaskSubmissionUseCase,
    private val updateSubmissionFromStudentUseCase: UpdateSubmissionFromStudentUseCase
) : BaseViewModel() {

    companion object {
        const val ALLOW_EDIT_TASK = "ALLOW_EDIT_TASK"
    }

    private val taskFlow = findTaskUseCase(taskId).shareIn(
        viewModelScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )
    val taskAttachments = findTaskAttachmentsUseCase(taskId)
    val taskViewState = taskFlow.onEach {
        if (it == null) {
            finish()
        }
    }
        .filterNotNull()
        .map { task ->
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
    val focusOnTextField = SingleLiveData<Unit>()

    private var oldContent = Task.Submission.Content.createEmpty()

    private var content = oldContent

    private lateinit var contentUpdateDate: LocalDateTime

    private val _submissionViewState = MutableSharedFlow<Task.Submission>(replay = 1)

    val submissionViewState2 = (_submissionViewState
        .map { it.toSubmissionViewState() })
        .shareIn(viewModelScope, SharingStarted.Lazily)


    init {
        viewModelScope.launch {
            if (findSelfUserUseCase().isStudent) {
                _submissionViewState.emitAll(
                    findSelfTaskSubmissionUseCase(task().id)
                        .onEach {
                            oldContent = it.content
                            content = oldContent
                            contentUpdateDate = it.contentUpdateDate
                        }
                )
            }
        }
    }

    val openFilePicker = SingleLiveData<Unit>()
    val openAttachment = SingleLiveData<File>()

    fun onBottomSheetStateChanged(newState: Int) {
        viewModelScope.launch {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                showSubmissionToolbar.value = false
                val submission = _submissionViewState.first()
                if (expandBottomSheet.value == BottomSheetBehavior.STATE_EXPANDED
                    && submission.status is Task.SubmissionStatus.NotSubmitted
                    && oldContent != content
                ) {
                    updateSubmissionFromStudentUseCase(submission)
                }
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                showSubmissionToolbar.value = true
            }
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED -> {
                    expandBottomSheet.value = newState
                }
            }
        }
    }

    fun onSubmissionAttachmentClick(position: Int) {
        viewModelScope.launch {
            openAttachment.value = _submissionViewState.first().content.attachments[position].file
        }
    }

    fun onAddAttachmentClick() = openFilePicker.call()


    fun onSelectedFile(file: File) {
        viewModelScope.launch {
            content = content.copy(attachments = content.attachments + Attachment(file))
            _submissionViewState.emit(_submissionViewState.first().copy(content = content))
        }
    }

    fun onTaskFileClick(position: Int) {
        viewModelScope.launch {
            openAttachment.value = task().attachments[position].file
        }
    }

    fun onRemoveSubmissionFileClick(position: Int) {
        viewModelScope.launch {
            content =
                content.copy(attachments = content.attachments - content.attachments[position])
            _submissionViewState.emit(_submissionViewState.first().copy(content = content))
        }
    }

    fun onSubmissionTextType(text: String) {
        viewModelScope.launch {
            content = content.copy(text = text)
            _submissionViewState.emit(_submissionViewState.first().copy(content = content))
        }
    }

    fun onBackPress() {
        if (expandBottomSheet.value != BottomSheetBehavior.STATE_COLLAPSED) {
            showSubmissionToolbar.value = false
            expandBottomSheet.value = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            viewModelScope.launch { finish() }
        }
    }

    fun onActionClick() {
        viewModelScope.launch {
            when (_submissionViewState.first().status) {
                is Task.SubmissionStatus.NotSubmitted,
                is Task.SubmissionStatus.Graded,
                is Task.SubmissionStatus.Rejected -> {
                    onSubmitActionClick()
                }
                is Task.SubmissionStatus.Submitted -> {
                    onCancelSubmitClick()
                }
            }
        }
    }

    private suspend fun onSubmitActionClick() {
        if (content.isEmpty()) {
            expandBottomSheet.value = BottomSheetBehavior.STATE_EXPANDED
            val submissionSettings = task().submissionSettings
            when {
                submissionSettings.onlyTextAvailable() -> {
                    focusOnTextField.call()
                }
                submissionSettings.onlyAttachmentsAvailable() || submissionSettings.allAvailable() -> {
                    openFilePicker.call()
                }
            }
        } else {
            val contentUpdateDate =
                if (contentIsChangeAndNotEmpty()) LocalDateTime.now()
                else this.contentUpdateDate

            _submissionViewState.emit(
                _submissionViewState.first().copy(
                    status = Task.SubmissionStatus.Submitted(),
                    contentUpdateDate = contentUpdateDate
                )
            )
            updateSubmissionFromStudentUseCase(_submissionViewState.first())
        }
    }

    private fun contentIsChangeAndNotEmpty() = oldContent != content && content.isNotEmpty()

    private suspend fun onCancelSubmitClick() {
        _submissionViewState.emit(
            _submissionViewState.first().copy(status = Task.SubmissionStatus.NotSubmitted)
        )
        updateSubmissionFromStudentUseCase(_submissionViewState.first())
    }

    private suspend fun task() = taskFlow.filterNotNull().first()

    data class TaskViewState(
        val name: String,
        val description: String,
        val dateWithTimeLeft: Pair<String, String>?,
        val submissionSettings: SubmissionSettings
    )

    data class SubmissionViewState(
        val btnVisibility: Boolean,
        val btnText: String = "",
        val btnTextColor: Int = R.color.white,
        val btnBackgroundColor: Int = R.color.blue,

        val title: String,
        val subtitleVisibility: Boolean = false,
        val subtitle: String = "",

        val teacher: User? = null,

        val textContent: String,
        val attachments: List<DomainModel>,
        val allowEditContent: Boolean,

        val submissionSettings: SubmissionSettings
    )

    private suspend fun Task.Submission.toSubmissionViewState(): SubmissionViewState {

        val submissionSettings = task().submissionSettings
        val allowEditContent = status !is Task.SubmissionStatus.Submitted

        val attachments =
            if (content.attachments.size == submissionSettings.attachmentsLimit || !allowEditContent)
                content.attachments
            else
                content.attachments + AddAttachmentItem

        return when (status) {
            Task.SubmissionStatus.NotSubmitted ->
                SubmissionViewState(
                    btnVisibility = true,
                    btnText = if (content.isEmpty()) "Добавить" else "Отправить",

                    title = "Не сдано",

                    submissionSettings = submissionSettings,

                    textContent = content.text,
                    attachments = attachments,
                    allowEditContent = allowEditContent,
                )

            is Task.SubmissionStatus.Submitted -> {
                val submittedDate = status.submittedDate
                val submittedDateText = task().completionDate?.let {
                    if (it > submittedDate) {
                        "вовремя: "
                    } else {
                        "с опозданием: "
                    } + submittedDate.toString("dd MMM HH:mm")
                } ?: run {
                    submittedDate.toString("dd MMM HH:mm")
                }


                SubmissionViewState(
                    btnVisibility = true,
                    btnText = "Отменить",
                    btnTextColor = R.color.red,
                    btnBackgroundColor = R.color.alpha_red_10,

                    title = "Сдано на проверку",
                    subtitleVisibility = true,
                    subtitle = submittedDateText,

                    submissionSettings = submissionSettings,

                    textContent = content.text,
                    attachments = attachments,
                    allowEditContent = allowEditContent,
                )
            }
            is Task.SubmissionStatus.Graded ->
                SubmissionViewState(
                    btnVisibility = contentIsChangeAndNotEmpty(),
                    btnText = "Отправить повторно",
                    title = "Оценено: ${status.grade}/5",

                    subtitleVisibility = false,

                    teacher = status.teacher,

                    submissionSettings = submissionSettings,

                    textContent = content.text,
                    attachments = attachments,
                    allowEditContent = allowEditContent,
                )

            is Task.SubmissionStatus.Rejected ->
                SubmissionViewState(
                    btnVisibility = contentIsChangeAndNotEmpty(),
                    btnText = "Отправить повторно",
                    title = "Отклонено",
                    subtitleVisibility = true,
                    subtitle = status.cause,

                    teacher = status.teacher,

                    submissionSettings = submissionSettings,

                    textContent = content.text,
                    attachments = attachments,
                    allowEditContent = allowEditContent,
                )
        }
    }

}