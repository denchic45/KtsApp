package com.denchic45.kts.ui.iconPicker

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class IconPickerViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val iconPickerInteractor: IconPickerInteractor
) : BaseViewModel() {

    val showIcons: StateFlow<List<Uri>> = flow {
        emit(subjectRepository.findAllRefsOfSubjectIcons())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onIconItemClick(position: Int) {
        viewModelScope.launch {
            iconPickerInteractor.postSelectedIcon(showIcons.value[position].toString())
            finish()
        }
    }
}