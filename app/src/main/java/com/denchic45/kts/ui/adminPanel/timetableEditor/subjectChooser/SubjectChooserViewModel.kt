package com.denchic45.kts.ui.adminPanel.timetableEditor.subjectChooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.domain.usecase.FindSubjectByTypedNameUseCase
import com.denchic45.kts.ui.base.chooser.ChooserViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubjectChooserViewModel @Inject constructor(
    private val subjectChooserInteractor: SubjectChooserInteractor,
    private val findSubjectByTypedNameUseCase: FindSubjectByTypedNameUseCase
) : ChooserViewModel<Subject>() {

    override val sourceFlow: (String) -> Flow<Resource<List<Subject>>> =
        findSubjectByTypedNameUseCase::invoke

    override fun onItemSelect(item: Subject) {
       viewModelScope.launch {
           subjectChooserInteractor.postSelectedSubject(item)
           finish()
       }
    }
}