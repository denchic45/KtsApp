package com.denchic45.kts.ui.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.FindSelfUserUseCase
import com.denchic45.kts.domain.usecase.RemoveCourseContentUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class CourseViewModel @Inject constructor(
    @Named(CourseFragment.COURSE_ID) private val courseId: String,
    findCourseUseCase: FindCourseUseCase,
    private val removeCourseContentUseCase: RemoveCourseContentUseCase,
    private val findCourseContentUseCase: FindCourseContentUseCase,
    findSelfUserUseCase: FindSelfUserUseCase
) : BaseViewModel() {

    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val openTaskEditor = SingleLiveData<Triple<String?, String, String>>()
    val openTask = SingleLiveData<Pair<String,String>>()
    val fabVisibility = SingleLiveData<Boolean>()

    val openCourseEditor = SingleLiveData<String>()
    val courseName: MutableLiveData<String> = MutableLiveData()
    val showContents: MutableLiveData<List<DomainModel>> = MutableLiveData()
    private val findCourseFlow = findCourseUseCase(courseId)

    private val uiPermissions: UiPermissions = UiPermissions(findSelfUserUseCase())

    init {
        viewModelScope.launch {
            findCourseContentUseCase.invoke(courseId).collect {
                showContents.value = it
            }
        }

        viewModelScope.launch {
            findCourseFlow.collect { course ->
                courseName.value = course.name
                uiPermissions.putPermissions(
                    Permission(ALLOW_COURSE_EDIT, { this == course.teacher }, { hasAdminPerms() }),
                    Permission(ALLOW_ADD_COURSE_CONTENT, { this == course.teacher }, { hasAdminPerms() })
                )
                val allowCourseEdit = uiPermissions.isNotAllowed(ALLOW_COURSE_EDIT)
                optionVisibility.value = R.id.option_edit_course to allowCourseEdit
                optionVisibility.value = R.id.option_edit_sections to allowCourseEdit

                fabVisibility.value = uiPermissions.isAllowed(ALLOW_ADD_COURSE_CONTENT)
            }
        }
    }

    fun onFabClick() {
        openTaskEditor.value = Triple(null, courseId, "")
    }

    fun onTaskItemClick(position: Int) {
        val task = showContents.value!![position] as Task
        openTask.value = task.id to task.courseId
    }

    fun onTaskItemLongClick(position: Int) {
//        viewModelScope.launch {
//            removeCourseContentUseCase(showContents.value!![position] as CourseContent)
//        }
    }


    override fun onCleared() {
        super.onCleared()
        findCourseContentUseCase.removeListeners()
    }

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_edit_course -> openCourseEditor.postValue(courseId)
        }
    }

    fun onCreateOptions() {

    }

    companion object {
        const val ALLOW_COURSE_EDIT = "PERMISSION_EDIT"
        const val ALLOW_ADD_COURSE_CONTENT = "ALLOW_ADD_COURSE_CONTENT"
    }
}