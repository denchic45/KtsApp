package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.ui.adminPanel.timetableEditor.subjectChooser.SubjectChooserInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class GroupSubjectChooserViewModel @Inject constructor(
    private val chooserInteractor: SubjectChooserInteractor
) : BaseViewModel() {

    val showSubjectsOfGroup: StateFlow<Resource<List<Subject>>> =
        chooserInteractor.subjectsOfGroup()
            .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Loading)

    val openIconPicker = SingleLiveData<Unit>()

    val openSubjectChooser = SingleLiveData<Unit>()

    fun onSubjectClick(position: Int) {
        viewModelScope.launch {
            chooserInteractor.postSelectedSubject(((showSubjectsOfGroup.value as Resource.Success).data[position]))
            finish()
        }
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_search_subject -> openSubjectChooser.call()
        }
    }

    init {
        toolbarTitle = "Предметы " + chooserInteractor.groupName
        viewModelScope.launch {
            chooserInteractor.receiveSelectedSubject()
            finish()
        }
    }
}