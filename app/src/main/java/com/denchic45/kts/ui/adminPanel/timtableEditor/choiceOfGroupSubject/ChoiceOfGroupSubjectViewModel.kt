package com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfGroupSubject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfSubject.ChoiceOfSubjectInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import javax.inject.Inject

class ChoiceOfGroupSubjectViewModel @Inject constructor(
    private val interactor: ChoiceOfSubjectInteractor
) : BaseViewModel() {

    val title = MutableLiveData<String>()

    val showSubjectsOfGroup: LiveData<Resource<List<Subject>>> = interactor.subjectsOfGroup

    val openIconPicker = SingleLiveData<Void>()

    val updateIconEventSubject = SingleLiveData<Void>()

    val openChoiceOfSubject = SingleLiveData<Void>()

    fun onSubjectClick(position: Int) {
        interactor.postSelectedSubject(showSubjectsOfGroup.value!!.data[position])
    }

    fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.option_search_subject -> openChoiceOfSubject.call()
        }
    }

    init {
        title.value = "Предметы " + interactor.groupName
        interactor.observeSelectedSubject().subscribe { finish.call() }
    }
}