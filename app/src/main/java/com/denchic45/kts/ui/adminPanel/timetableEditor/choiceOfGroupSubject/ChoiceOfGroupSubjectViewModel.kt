package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject.ChoiceOfSubjectInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ChoiceOfGroupSubjectViewModel @Inject constructor(
    private val interactor: ChoiceOfSubjectInteractor
) : BaseViewModel() {

    val title = MutableLiveData<String>()

    val showSubjectsOfGroup: StateFlow<Resource<List<Subject>>> =
        interactor.subjectsOfGroup()
            .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Loading)

    val openIconPicker = SingleLiveData<Void>()

    val updateIconEventSubject = SingleLiveData<Void>()

    val openChoiceOfSubject = SingleLiveData<Void>()

    fun onSubjectClick(position: Int) {
        interactor.postSelectedSubject(((showSubjectsOfGroup.value as Resource.Success).data[position]))
    }

    fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.option_search_subject -> openChoiceOfSubject.call()
        }
    }

    init {
        title.value = "Предметы " + interactor.groupName
        interactor.observeSelectedSubject().subscribe { finish() }
    }
}