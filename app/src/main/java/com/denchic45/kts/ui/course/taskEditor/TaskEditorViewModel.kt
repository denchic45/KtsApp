package com.denchic45.kts.ui.course.taskEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.utils.UUIDS
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TaskEditorViewModel @Inject constructor(
    @Named(TaskEditorFragment.TASK_ID) taskId: String?
) :
    ViewModel() {

    private val taskId: String
    val titleField = MutableLiveData<String>()
    val descriptionField = MutableLiveData<String>()
    val availabilityDateField = MutableLiveData<String?>()

    val availabilityDateRemoveVisibility = MutableLiveData<Boolean>()
    val showFiles = MutableLiveData<List<Attachment>>()
    val filesVisibility = MutableLiveData<Boolean>()

    val openFileChooser = SingleLiveData<Unit>()

    var date: LocalDateTime? = null
    private val files: MutableList<Attachment> = mutableListOf()

    val openDatePicker: SingleLiveData<Long> = SingleLiveData()
    val openTimePicker: SingleLiveData<Pair<Int, Int>> = SingleLiveData()

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
        }
            ?: kotlin.run {
                availabilityDateField.postValue(null)
            }
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
        files.addAll(selectedFiles.map { Attachment(it) })
        showFiles.postValue(files)
    }
}