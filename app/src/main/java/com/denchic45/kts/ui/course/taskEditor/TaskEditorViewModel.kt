package com.denchic45.kts.ui.course.taskEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.data.model.domain.SubmissionSettings
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.UUIDS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class TaskEditorViewModel @Inject constructor(
    @Named(TaskEditorFragment.TASK_ID) taskId: String?,
    @Named(TaskEditorFragment.COURSE_ID) private val courseId: String,
    @Named(TaskEditorFragment.SECTION_ID) sectionId: String?,
    private val confirmInteractor: ConfirmInteractor,
    private val findTaskUseCase: FindTaskUseCase,
    private val findAttachmentsUseCase: FindAttachmentsUseCase,
    private val findSectionUseCase: FindSectionUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase
) : BaseViewModel() {

    val nameField = MutableLiveData<String>()
    val descriptionField = MutableLiveData<String>()
    val showCompletionDate = MutableLiveData<String?>()
    val disabledSendAfterDate = MutableLiveData<Boolean>()
    val sectionField = MutableLiveData<String>()

    val availabilityDateRemoveVisibility = MutableLiveData<Boolean>()
    val showAttachments = MutableLiveData<List<Attachment>>()
    val openCourseSections = SingleLiveData<Pair<String, Section>>()
    val filesVisibility = MutableLiveData(false)
    val commentsEnabled = MutableLiveData<Boolean>()

    val submissionSettings = MutableStateFlow(
        SubmissionSettingsState(
            true,
            "500",
            false,
            "16",
            "200"
        )
    )
    val openFileChooser = SingleLiveData<Unit>()
    val openAttachment = SingleLiveData<File>()
    val openDatePicker: SingleLiveData<Long> = SingleLiveData()
    val openTimePicker: SingleLiveData<Pair<Int, Int>> = SingleLiveData()
    val showErrorMessage = SingleLiveData<Pair<Int, String?>>()

    private var completionDate: LocalDateTime? = null
    private val attachments: MutableList<Attachment> = mutableListOf()
    private val taskId: String = taskId ?: UUIDS.createShort()
    private var section: Section = Section.createEmpty()

    private var createdDate: Date = Date()
    private var timestamp: Date = Date()

    private var order: Long = 0

    private val uiEditor: UIEditor<Task> = UIEditor(taskId == null) {
        Task(
            this.taskId,
            courseId,
            this.section.id,
            nameField.value ?: "",
            descriptionField.value ?: "",
            order,
            if (showCompletionDate.value != null) LocalDateTime.parse(
                showCompletionDate.value,
                DateTimeFormatter.ofPattern("EE, dd LLLL yyyy, HH:mm")
            ) else null,
            disabledSendAfterDate.value ?: false,
            attachments,
            with(submissionSettings.value) {
                SubmissionSettings(
                    textAvailable,
                    charsLimit.toInt(),
                    attachmentsAvailable,
                    attachmentsLimit.toInt(),
                    attachmentsSizeLimit.toInt()
                )
            },
            commentsEnabled.value ?: false,
            createdDate,
            timestamp
        )
    }

    private val uiValidator: UIValidator = UIValidator.of(
        Validation(
            Rule({ !nameField.value.isNullOrEmpty() }, "Название задания обязательно!")
        ).sendMessageResult(R.id.til_name, showErrorMessage),
        Validation(
            Rule({
                with(submissionSettings.value) {
                    attachmentsAvailable || textAvailable
                }
            }, "Должен быть выбран хотя бы один вариант ответа")
        ),
        Validation(
            Rule(
                {
                    with(submissionSettings.value) {
                        if (textAvailable) {
                            charsLimit.isNotEmpty() && charsLimit != "0"
                        } else true
                    }
                }, "Недопустимое число символов!"
            )
        ).sendMessageResult(R.id.et_chars_limit, showErrorMessage),
        Validation(
            Rule({
                with(submissionSettings.value) {
                    if (attachmentsAvailable) {
                        attachmentsLimit.isNotEmpty() && attachmentsLimit != "0"
                    } else true
                }
            }, "Недопустимое количество файлов!")

        ).sendMessageResult(R.id.et_attachments_limit, showErrorMessage),
        Validation(
            Rule({
                with(submissionSettings.value) {
                    if (attachmentsAvailable) {
                        attachmentsSizeLimit.isNotEmpty() && attachmentsSizeLimit != "0"
                    } else true
                }
            }, "Недопустимый размер для файлов!")
        ).sendMessageResult(R.id.et_attachments_size_limit, showErrorMessage)
    )

    init {

        if (uiEditor.isNew) {
            setupForNew()
        } else setupForExist()

        availabilityDateRemoveVisibility.postValue(completionDate != null)
    }


    private fun setupForNew() {

    }

    private fun setupForExist() {
        viewModelScope.launch {
            findTaskUseCase(taskId)
                .onEach { if (it == null)  finish() }
                .filterNotNull()
                .collect {
                    uiEditor.oldItem = it
                    nameField.value = it.name
                    descriptionField.value = it.description
                    completionDate = it.completionDate
                    createdDate = it.createdDate
                    timestamp = it.timestamp
                    postCompletionDate()
                    disabledSendAfterDate.value = it.disabledSendAfterDate
                    submissionSettings.value = with(it.submissionSettings) {
                        SubmissionSettingsState(
                            textAvailable,
                            charsLimit.toString(),
                            attachmentsAvailable,
                            attachmentsLimit.toString(),
                            attachmentsSizeLimit.toString()
                        )
                    }
                    commentsEnabled.value = it.commentsEnabled
                    section = findSectionUseCase(it.sectionId)
                    postSelection()
                    observeAttachments()
                }
        }

    }

    private fun observeAttachments() {
        viewModelScope.launch {
            findAttachmentsUseCase(taskId).collect {
                attachments.clear()
                attachments.addAll(it)
                uiEditor.oldItem = uiEditor.oldItem!!.copy(attachments = attachments.toList())
                postAttachments()
            }
        }
    }

    fun onAvailabilityDateClick() {
        completionDate?.let {
            openDatePicker.postValue(it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        } ?: run {
            openDatePicker.postValue(System.currentTimeMillis())
        }
    }

    fun onAvailabilityDateSelect(milliseconds: Long) {
        availabilityDateRemoveVisibility.postValue(true)
        val dateIsNull = completionDate == null
        completionDate =
            Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (dateIsNull)
            completionDate = completionDate!!.withHour(23).withMinute(59).withSecond(0)
        openTimePicker.postValue(completionDate!!.hour to completionDate!!.minute)

        postCompletionDate()
    }

    private fun postCompletionDate() {
        completionDate?.let {
            showCompletionDate.postValue(
                completionDate!!.format(DateTimeFormatter.ofPattern("EE, dd LLLL yyyy, HH:mm"))
            )
        } ?: kotlin.run {
            showCompletionDate.postValue(null)
        }
    }

    private fun postAttachments() {
        filesVisibility.postValue(attachments.isNotEmpty())
        showAttachments.postValue(attachments)
    }

    fun onAvailabilityTimeSelect(hour: Int, minute: Int) {
        completionDate = completionDate!!.withHour(hour).withMinute(minute)
        postCompletionDate()
    }

    fun onRemoveAvailabilityDate() {
        completionDate = null
        availabilityDateRemoveVisibility.postValue(false)
        postCompletionDate()
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_attachment -> {
                openFileChooser.call()
            }
            R.id.option_save_task -> onSaveClick()
        }
    }

    fun onAttachmentsSelect(selectedFiles: List<File>) {
        attachments.addAll(selectedFiles.map { Attachment(file = it) })
        postAttachments()
    }

    fun onRemoveFileClick(position: Int) {
        openConfirmation("Убрать файл" to "подтвердите ваш выбор")
        viewModelScope.launch {
            if (confirmInteractor.receiveConfirm()) {
                attachments.removeAt(position)
                postAttachments()
            }
        }
    }

    fun onAttachmentClick(position: Int) {
        openAttachment.postValue(attachments[position].file)
    }

    fun onAvailabilitySendCheck(check: Boolean) {
        disabledSendAfterDate.postValue(check)
    }

    fun onTextAvailableAnswerCheck(check: Boolean) {
        submissionSettings.value.apply {
            val availableAttachmentsIfAllOptionsDisabled =
                !check && !attachmentsAvailable || attachmentsAvailable
            submissionSettings.value = copy(
                textAvailable = check,
                attachmentsAvailable = availableAttachmentsIfAllOptionsDisabled
            )
        }
    }

    fun onAttachmentsAvailableAnswerCheck(check: Boolean) {
        submissionSettings.value.apply {
            val availableTextIfAllOptionsDisabled =
                !check && !textAvailable || textAvailable
            submissionSettings.value = copy(
                attachmentsAvailable = check,
                textAvailable = availableTextIfAllOptionsDisabled
            )
        }
    }

    fun onNameType(name: String) = nameField.postValue(name)


    fun onDescriptionType(name: String) = descriptionField.postValue(name)


    fun onCharsLimitType(charsLimit: String) {
        submissionSettings.value = submissionSettings.value.copy(charsLimit = charsLimit)
    }

    fun onAttachmentsLimitType(attachmentsLimit: String) {
        submissionSettings.value =
            submissionSettings.value.copy(attachmentsLimit = attachmentsLimit)
    }

    fun onAttachmentsSizeLimitType(attachmentsSizeLimit: String) {
        submissionSettings.value =
            submissionSettings.value.copy(attachmentsSizeLimit = attachmentsSizeLimit)
    }

    fun onCommentsEnableCheck(check: Boolean) {
        commentsEnabled.postValue(check)
    }

    private fun onSaveClick() {
        if (uiValidator.runValidates() && uiEditor.hasBeenChanged()) {
            viewModelScope.launch {
                if (uiEditor.isNew) {
                    addTaskUseCase(uiEditor.item)
                } else {
                    updateTaskUseCase(uiEditor.item)
                }
                 finish()
            }
        }
    }

    fun onSectionClick() {
        openCourseSections.value = courseId to section
    }

    fun onSectionSelected(section: Section) {
        this.section = section
        postSelection()
    }

    private fun postSelection() {
        sectionField.value = section.name
    }

    data class SubmissionSettingsState(
        val textAvailable: Boolean,
        val charsLimit: String,
        val attachmentsAvailable: Boolean,
        val attachmentsLimit: String,
        val attachmentsSizeLimit: String
    )
}