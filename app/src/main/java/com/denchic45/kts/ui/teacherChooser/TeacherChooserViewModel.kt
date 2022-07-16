package com.denchic45.kts.ui.teacherChooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.domain.usecase.FindTeacherByContainsNameUseCase
import com.denchic45.kts.ui.base.chooser.ChooserViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class TeacherChooserViewModel @Inject constructor(
    private val teacherChooserInteractor: TeacherChooserInteractor,
    findTeacherByContainsNameUseCase: FindTeacherByContainsNameUseCase
) : ChooserViewModel<User>() {

    override val sourceFlow = findTeacherByContainsNameUseCase::invoke

    override fun onItemSelect(item: User) {
        viewModelScope.launch {
            teacherChooserInteractor.postSelectedCurator(item)
            finish()
        }
    }

}