package com.denchic45.kts.ui.teacherChooser

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.base.chooser.ChooserViewModel
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import javax.inject.Inject

class TeacherChooserViewModel @Inject constructor(
    private val choiceOfCuratorInteractor: ChoiceOfCuratorInteractor
) : ChooserViewModel<User>() {

    override val sourceFlow = choiceOfCuratorInteractor::findTeacherByTypedName

    override fun onItemSelect(item: User) {
        choiceOfCuratorInteractor.postSelectedCurator(item)
        finish()
    }

}