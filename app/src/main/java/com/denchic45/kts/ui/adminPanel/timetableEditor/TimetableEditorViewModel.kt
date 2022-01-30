package com.denchic45.kts.ui.adminPanel.timetableEditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

class TimetableEditorViewModel @Inject constructor(
    application: Application,
    private val interactor: TimetableEditorInteractor
) : AndroidViewModel(application) {
    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }
}