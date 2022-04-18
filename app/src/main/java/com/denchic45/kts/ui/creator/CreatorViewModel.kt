package com.denchic45.kts.ui.creator

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.ui.UiImage
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.userEditor.UserEditorFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreatorViewModel @Inject constructor() : BaseViewModel() {
    var openUserEditor = SingleLiveData<Map<String, String>>()
    var openGroupEditor = SingleLiveData<Void>()
    var openSubjectEditor = SingleLiveData<Void>()
    var openSpecialtyEditor = SingleLiveData<Void>()
    var openCourseEditor = SingleLiveData<Void>()
    fun onEntityClick(position: Int) {
        val argsMap: MutableMap<String, String> = HashMap()
        when (position) {
            0 -> {
                argsMap[UserEditorFragment.USER_ROLE] = User.STUDENT
                openUserEditor.setValue(argsMap)
            }
            1 -> {
                argsMap[UserEditorFragment.USER_ROLE] = User.TEACHER
                openUserEditor.setValue(argsMap)
            }
            2 -> openGroupEditor.call()
            3 -> openSubjectEditor.call()
            4 -> openSpecialtyEditor.call()
            5 -> openCourseEditor.call()
        }
        viewModelScope.launch { finish() }
    }

    fun createEntityList(): List<ListItem> {
        return listOf(
            ListItem(title = "Студента", icon = UiImage.IdImage(R.drawable.ic_student)),
            ListItem(title = "Преподавателя", icon = UiImage.IdImage(R.drawable.ic_teacher)),
            ListItem(title = "Группу", icon = UiImage.IdImage(R.drawable.ic_group)),
            ListItem(title = "Предмет", icon = UiImage.IdImage(R.drawable.ic_subject)),
            ListItem(title = "Специальность", icon = UiImage.IdImage(R.drawable.ic_specialty)),
            ListItem(title = "Курс", icon = UiImage.IdImage(R.drawable.ic_course))
        )
    }
}