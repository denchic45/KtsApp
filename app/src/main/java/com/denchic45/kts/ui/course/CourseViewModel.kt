package com.denchic45.kts.ui.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UIPermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class CourseViewModel @Inject constructor(
    @Named(CourseFragment.COURSE_UUID) private val courseId: String,
    private val findUserUseCase: FindUserUseCase,
    private val findCourseUseCase: FindCourseUseCase,
    private val findContentCourseUseCase: FindContentCourseUseCase
) : ViewModel() {

    val courseName: MutableLiveData<String> = MutableLiveData()
    val showContents: MutableLiveData<List<DomainModel>> = MutableLiveData()

    private val findCourseFlow = findCourseUseCase(courseId)

    init {
        viewModelScope.launch {
            findCourseFlow.collect { course ->
                courseName.value = course.info.name
                uiPermissions.addPermissions(Permission(
                    PERMISSION_EDIT,
                    { it == course.info.teacher },
                    { it.admin }
                )
                )
            }
        }
        viewModelScope.launch {
            findContentCourseUseCase.invoke(courseId).collect {
                showContents.value = it
            }
        }
    }

    private val uiPermissions: UIPermissions = UIPermissions(findUserUseCase()).apply {

    }


    companion object {
        const val PERMISSION_EDIT = "PERMISSION_EDIT"
    }
}