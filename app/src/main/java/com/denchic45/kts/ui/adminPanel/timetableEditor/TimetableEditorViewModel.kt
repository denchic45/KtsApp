package com.denchic45.kts.ui.adminPanel.timetableEditor

import com.denchic45.kts.ui.base.BaseViewModel
import javax.inject.Inject

class TimetableEditorViewModel @Inject constructor(
    private val interactor: TimetableEditorInteractor
) : BaseViewModel() {
    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }
}