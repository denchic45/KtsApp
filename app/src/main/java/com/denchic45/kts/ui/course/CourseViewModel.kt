package com.denchic45.kts.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UIPermissions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class CourseViewModel @Inject constructor(
    @Named(CourseFragment.COURSE_UUID) private val courseId: String,
    private val findUserUseCase: FindUserUseCase,
    private val findCourseUseCase: FindCourseUseCase
) : ViewModel() {

    private val findCourseFlow = findCourseUseCase(courseId)

    init {
        viewModelScope.launch {
            findCourseFlow.collect { course ->
                uiPermissions.addPermissions(Permission(
                    PERMISSION_EDIT,
                    { it == course.info.teacher },
                    { it.admin }
                )
                )
            }
        }
    }

    private val uiPermissions: UIPermissions = UIPermissions(findUserUseCase()).apply {

    }


    companion object {
        const val PERMISSION_EDIT = "PERMISSION_EDIT"
    }
}