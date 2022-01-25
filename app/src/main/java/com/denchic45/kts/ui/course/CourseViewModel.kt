package com.denchic45.kts.ui.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.RemoveCourseContentUseCase
import com.denchic45.kts.domain.usecase.FindSelfUserUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UIPermissions
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

    val openTaskEditor = SingleLiveData<Triple<String?, String, String>>()
    val openTask = SingleLiveData<String>()

    val openCourseEditor = SingleLiveData<String>()
    val courseName: MutableLiveData<String> = MutableLiveData()
    val showContents: MutableLiveData<List<DomainModel>> = MutableLiveData()
    private val findCourseFlow = findCourseUseCase(courseId)

    private val uiPermissions: UIPermissions = UIPermissions(findSelfUserUseCase())

    init {
        viewModelScope.launch {
            findCourseFlow.collect { course ->
                courseName.value = course.name
                uiPermissions.addPermissions(
                    Permission(
                    PERMISSION_EDIT,
                    { it == course.teacher },
                    { it.admin }
                )
                )
            }
        }
        viewModelScope.launch {
            findCourseContentUseCase.invoke(courseId).collect {
                showContents.value = it
            }
        }

    }

    fun onFabClick() {
        openTaskEditor.value = Triple(null, courseId, "")
    }

    fun onTaskItemClick(position: Int) {
        openTask.value = (showContents.value!![position] as Task).id

        //open task editor
//        openTaskEditor.value = Triple(
//            showContents.value!![position].id,
//            courseId,
//            (showContents.value!![position] as Task).sectionId
//        )
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

    companion object {
        const val PERMISSION_EDIT = "PERMISSION_EDIT"
    }
}