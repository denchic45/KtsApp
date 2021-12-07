package com.denchic45.kts.ui.iconPicker

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.repository.SubjectRepository
import javax.inject.Inject

class IconPickerViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val iconPickerInteractor: IconPickerInteractor
) : ViewModel() {
    @JvmField
    val showIcons = MutableLiveData<List<Uri>>()

    @JvmField
    val finish = SingleLiveData<Void>()

    fun onIconItemClick(position: Int) {
        finish.call()
        iconPickerInteractor.postSelectedIcon(showIcons.value!![position].toString())
    }

    init {
        subjectRepository.findAllRefsOfSubjectIcons()
            .subscribe { value: List<Uri> -> showIcons.setValue(value) }
    }
}