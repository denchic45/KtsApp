package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChoiceOfSubjectViewModel @Inject constructor(
    private val interactor: ChoiceOfSubjectInteractor
) : BaseViewModel() {
    val showFoundSubjects = MutableLiveData<Resource<List<Subject>>>()
    private val querySubjectsByName = MutableSharedFlow<String>()

    fun onSubjectNameType(name: String) {
        viewModelScope.launch {
            querySubjectsByName.emit(name)
        }
    }

    fun onSubjectClick(position: Int) {
        interactor.postSelectedSubject((showFoundSubjects.value!! as Resource.Success).data[position])
        finish.call()
    }

    init {
        viewModelScope.launch {
            querySubjectsByName.filter { s: String -> s.length > 2 }
                .flatMapLatest { name: String -> interactor.findSubjectByTypedName(name) }
                .collect { value: Resource<List<Subject>> -> showFoundSubjects.setValue(value) }
        }
    }
}