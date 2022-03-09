package com.denchic45.kts.ui.teacherChooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.domain.usecase.FindTeacherByTypedNameUseCase
import com.denchic45.kts.ui.base.chooser.ChooserViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TeacherChooserViewModel @Inject constructor(
    private val teacherChooserInteractor: TeacherChooserInteractor,
    findTeacherByTypedNameUseCase: FindTeacherByTypedNameUseCase
) : ChooserViewModel<User>() {

    override val sourceFlow = findTeacherByTypedNameUseCase::invoke

    override fun onItemSelect(item: User) {
        viewModelScope.launch {
            teacherChooserInteractor.postSelectedCurator(item)
            finish()
        }
    }

}