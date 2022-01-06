package com.denchic45.kts.ui.course.taskEditor

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.UUIDS
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TaskEditorViewModel @Inject constructor(
    @Named(TaskEditorFragment.TASK_ID) taskId: String?
) : BaseViewModel() {
    val titleField = MutableLiveData<String>()
    val descriptionField = MutableLiveData<String>()
    val availabilityDateField = MutableLiveData<String?>()
    val availabilitySend = MutableLiveData<Boolean>()

    val availabilityDateRemoveVisibility = MutableLiveData<Boolean>()
    val showAttachments = MutableLiveData<List<Attachment>>()
    val filesVisibility = MutableLiveData<Boolean>()
    val commentsEnabled = MutableLiveData<Boolean>()

    val answerType = MutableStateFlow(
        AnswerTypeState(
            true,
            "9999",
            false,
            "16",
            "200"
        )
    )

    val markType = MutableStateFlow<MarkTypeState>(MarkTypeState.Score("5"))

    val openFileChooser = SingleLiveData<Unit>()
    val openAttachment = SingleLiveData<File>()
    val openDatePicker: SingleLiveData<Long> = SingleLiveData()
    val openTimePicker: SingleLiveData<Pair<Int, Int>> = SingleLiveData()

    var date: LocalDateTime? = null
    private val attachments: MutableList<Attachment> = mutableListOf()
    private val taskId: String

    init {
        if (taskId != null) {
            this.taskId = taskId
            setupForExist()
        } else
            this.taskId = UUIDS.createShort()
        setupForNew()

        availabilityDateRemoveVisibility.postValue(date != null)
        filesVisibility.postValue(false) //todo временно
    }

    private fun setupForNew() {

    }

    private fun setupForExist() {

    }

    fun onAvailabilityDateClick() {
        date?.let {
            openDatePicker.postValue(it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        } ?: run {
            openDatePicker.postValue(System.currentTimeMillis())
        }
    }

    fun onAvailabilityDateSelect(milliseconds: Long) {
        availabilityDateRemoveVisibility.postValue(true)
        val dateIsNull = date == null
        date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (dateIsNull)
            date = date!!.withHour(23).withMinute(59).withSecond(0)
        openTimePicker.postValue(date!!.hour to date!!.minute)

        postAvailabilityDate()
    }

    private fun postAvailabilityDate() {
        date?.let {
            availabilityDateField.postValue(
                date!!.format(DateTimeFormatter.ofPattern("EE, dd LLLL yyyy, HH:mm"))
            )
        } ?: kotlin.run {
            availabilityDateField.postValue(null)
        }
    }

    private fun postAttachments() {
        filesVisibility.postValue(attachments.isNotEmpty())
        showAttachments.postValue(attachments)
    }

    fun onAvailabilityTimeSelect(hour: Int, minute: Int) {
        date = date!!.withHour(hour).withMinute(minute)
        postAvailabilityDate()
    }

    fun onRemoveAvailabilityDate() {
        date = null
        availabilityDateRemoveVisibility.postValue(false)
        postAvailabilityDate()
    }

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_attachment -> {
                openFileChooser.call()
            }
            R.id.option_save_task -> {}
        }
    }

    fun onAttachmentsSelect(selectedFiles: List<File>) {
        selectedFiles[0]
        attachments.addAll(selectedFiles.map { Attachment(file = it) })
        postAttachments()
    }

    fun onRemoveFileClick(position: Int) {
        openConfirmation.postValue("Убрать файл" to "подтвердите ваш выбор")
        RxBusConfirm.getInstance()
            .event
            .subscribe {
                if (it) {
                    attachments.removeAt(position)
                    postAttachments()
                }
            }
    }

    fun onAttachmentClick(position: Int) {
        openAttachment.postValue(attachments[position].file)
    }

    fun onAvailabilitySendCheck(check: Boolean) {
        availabilitySend.postValue(check)
    }

    fun onTextAvailableAnswerCheck(check: Boolean) {
        answerType.value = answerType.value.copy(textAvailable = check)
    }

    fun onAttachmentsAvailableAnswerCheck(check: Boolean) {
        answerType.value = answerType.value.copy(attachmentsAvailable = check)
    }

    fun onNameType(name: String) {
        titleField.postValue(name)
    }

    fun onDescriptionType(name: String) {
        descriptionField.postValue(name)
    }

    fun onCharsLimitType(charsLimit: String) {
        answerType.value = answerType.value.copy(charsLimit = charsLimit)
    }

    fun onAttachmentsLimitType(attachmentsLimit: String) {
        answerType.value = answerType.value.copy(attachmentsLimit = attachmentsLimit)
    }

    fun onAttachmentsSizeLimitType(attachmentsSizeLimit: String) {
        answerType.value = answerType.value.copy(attachmentsSizeLimit = attachmentsSizeLimit)
    }

    fun onMaxScoreType(maxScore: String) {
        markType.value.apply {
            if (this is MarkTypeState.Score)
                markType.value = copy(max = maxScore)
        }
    }

    fun onMarkTypeSelect(position: Int) {
        when (position) {
            0 -> {
                if (markType.value.position != 0) {
                    markType.value = MarkTypeState.Score("5")
                }
            }
            1 -> markType.value = MarkTypeState.Binary
        }
    }

    fun onCommentsEnableCheck(check: Boolean) {
        commentsEnabled.postValue(check)
    }

    data class AnswerTypeState(
        val textAvailable: Boolean,
        val charsLimit: String,
        val attachmentsAvailable: Boolean,
        val attachmentsLimit: String,
        val attachmentsSizeLimit: String
    )

    sealed class MarkTypeState {
        abstract val position: Int

        data class Score(val max: String) : MarkTypeState() {
            override val position: Int get() = 0
        }

        object Binary : MarkTypeState() {
            override val position: Int get() = 1
        }
    }
}