package com.denchic45.kts.ui.iconPicker

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.ui.base.BaseViewModel
import javax.inject.Inject

class IconPickerViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val iconPickerInteractor: IconPickerInteractor
) : BaseViewModel() {

    val showIcons = MutableLiveData<List<Uri>>()

    fun onIconItemClick(position: Int) {
        iconPickerInteractor.postSelectedIcon(showIcons.value!![position].toString())
        finish()
    }

    init {
        subjectRepository.findAllRefsOfSubjectIcons()
            .subscribe { value: List<Uri> -> showIcons.setValue(value) }
    }
}