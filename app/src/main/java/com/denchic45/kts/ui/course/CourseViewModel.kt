package com.denchic45.kts.ui.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.CourseContent
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import com.denchic45.kts.utils.Orders
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class CourseViewModel @Inject constructor(
    @Named(CourseFragment.COURSE_ID) private val courseId: String,
    findCourseUseCase: FindCourseUseCase,
    private val removeCourseContentUseCase: RemoveCourseContentUseCase,
    private val updateCourseContentOrderUseCase: UpdateCourseContentOrderUseCase,
    val findCourseContentUseCase: FindCourseContentUseCase,
    findSelfUserUseCase: FindSelfUserUseCase
) : BaseViewModel() {

    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val openTaskEditor = SingleLiveData<Triple<String?, String, String>>()
    val openTask = SingleLiveData<Pair<String, String>>()
    val fabVisibility = SingleLiveData<Boolean>()

    val openCourseEditor = SingleLiveData<String>()
    val openCourseSectionEditor = SingleLiveData<String>()
    val courseName: MutableLiveData<String> = MutableLiveData()
    val showContents: MutableLiveData<MutableList<DomainModel>> = MutableLiveData()
    private val findCourseFlow = findCourseUseCase(courseId)

    private val uiPermissions: UiPermissions = UiPermissions(findSelfUserUseCase())

    private var oldPosition: Int = -1
    private var position: Int = -1

    init {
        viewModelScope.launch {
            findCourseContentUseCase.invoke(courseId).collect {
//                it.filterIsInstance<CourseContent>()
//                    .forEach {
//                        Log.d("lol", "get contents: ${it.name} ${it.order}")
//                    }
                showContents.value = it.toMutableList()
            }
        }

        viewModelScope.launch {
            findCourseFlow.collect { course ->
                courseName.value = course.name
                uiPermissions.putPermissions(
                    Permission(ALLOW_COURSE_EDIT, { this == course.teacher }, { hasAdminPerms() }),
                    Permission(
                        ALLOW_ADD_COURSE_CONTENT,
                        { this == course.teacher },
                        { hasAdminPerms() })
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

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_edit_course -> openCourseEditor.value = courseId
            R.id.option_edit_sections -> openCourseSectionEditor.value = courseId
        }
    }

    fun onContentMove(oldPosition: Int, position: Int) {
        if (this.oldPosition == -1)
            this.oldPosition = oldPosition
        this.position = position

        Log.d("lol", "onContentMove swap: $oldPosition $position")
        Collections.swap(showContents.value!!, oldPosition, position)

        showContents.value = showContents.value
    }

    fun onContentMoved() {
        Log.d("lol", "onContentMoved: ${oldPosition == position}")
        if (oldPosition == position)
            return
        Log.d("lol", "onContentMoved: $oldPosition $position")


        viewModelScope.launch {
            val contents = showContents.value!!

            val prevOrder = if (position == 0 || contents[position - 1] !is CourseContent) 0
            else (contents[position - 1] as CourseContent).order

            val nextOrder =
                if (position == contents.size - 1) (contents[position - 1] as CourseContent).order + (1024 * 2)
                else (contents[position + 1] as CourseContent).order

            print(contents)

            updateCourseContentOrderUseCase(
                contents[position].id,
                Orders.getBetweenOrders(prevOrder, nextOrder)
            )
            oldPosition = -1
            position = -1
        }

    }

    companion object {
        const val ALLOW_COURSE_EDIT = "PERMISSION_EDIT"
        const val ALLOW_ADD_COURSE_CONTENT = "ALLOW_ADD_COURSE_CONTENT"
    }
}