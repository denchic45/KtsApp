package com.denchic45.kts.ui.creater

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.EitherResource
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import java.util.*

class CreatorViewModel(application: Application) : AndroidViewModel(application) {
    var openUserEditor = SingleLiveData<Map<String, String>>()
    var openGroupEditor = SingleLiveData<Void>()
    var openSubjectEditor = SingleLiveData<Void>()
    var openSpecialtyEditor = SingleLiveData<Void>()
    var openCourseEditor = SingleLiveData<Void>()
    var finish = SingleLiveData<Void>()
    fun onEntityClick(position: Int) {
        val argsMap: MutableMap<String, String> = HashMap()
        when (position) {
            0 -> {
                argsMap[UserEditorActivity.USER_ROLE] = User.STUDENT
                openUserEditor.setValue(argsMap)
            }
            1 -> {
                argsMap[UserEditorActivity.USER_ROLE] = User.TEACHER
                openUserEditor.setValue(argsMap)
            }
            2 -> openGroupEditor.call()
            3 -> openSubjectEditor.call()
            4 -> openSpecialtyEditor.call()
            5 -> openCourseEditor.call()
        }
        finish.call()
    }

    fun createEntityList(): List<ListItem> {
        return Arrays.asList(
            ListItem(title = "Студента", icon = EitherResource.Id(R.drawable.ic_student)),
            ListItem(title = "Преподавателя", icon = EitherResource.Id(R.drawable.ic_teacher)),
            ListItem(title = "Группу", icon = EitherResource.Id(R.drawable.ic_group)),
            ListItem(title = "Предмет", icon = EitherResource.Id(R.drawable.ic_subject)),
            ListItem(title = "Специальность", icon = EitherResource.Id(R.drawable.ic_specialty)),
            ListItem(title = "Курс", icon = EitherResource.Id(R.drawable.ic_course))
        )
    }
}