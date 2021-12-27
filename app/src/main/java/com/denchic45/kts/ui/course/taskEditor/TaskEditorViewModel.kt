package com.denchic45.kts.ui.course.taskEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denchic45.kts.SingleLiveData
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TaskEditorViewModel @Inject constructor() : ViewModel() {

    val titleField = MutableLiveData<String>()
    val descriptionField = MutableLiveData<String>()
    val dateField = MutableLiveData<String?>()
    var date: LocalDateTime? = null

    val openDatePicker: SingleLiveData<Long> = SingleLiveData()
    val openTimePicker: SingleLiveData<Pair<Int, Int>> = SingleLiveData()

    fun onAvailabilityDateClick() {
        date?.let {
            openDatePicker.postValue(it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }?: run {
            openDatePicker.postValue(System.currentTimeMillis())
        }
    }

    fun onAvailabilityDateSelect(milliseconds: Long) {
        val dateIsNull = date == null
        date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
            .apply {
                if (dateIsNull)
                   date = withHour(23).withMinute(59).withSecond(0)
                openTimePicker.postValue(date!!.hour to date!!.minute)
            }
        postAvailabilityDate()
    }

    private fun postAvailabilityDate() {
        dateField.postValue(date!!.format(DateTimeFormatter.ofPattern("EEEE, dd LLLL yyyy, HH:mm")))
    }

    fun onAvailabilityTimeSelect(hour: Int, minute: Int) {
        date = date!!.withHour(hour).withMinute(minute)
        postAvailabilityDate()
    }
}