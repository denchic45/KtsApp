package com.denchic45.kts.ui.group.courses

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.ui.base.NavigationCommand
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.course.CourseFragmentDirections
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupCoursesViewModel @Inject constructor(
    interactor: GroupCoursesInteractor,
    @Named(GroupCoursesFragment.GROUP_ID) groupId: String?
) : BaseViewModel() {
    val clearItemsSelection = SingleLiveData<Set<Int>>()
    val selectItem = MutableLiveData<Pair<Int, Boolean>>()
    private val groupId: String = groupId ?: interactor.yourGroupId
    var courses: StateFlow<List<CourseHeader>> =
        interactor.findCoursesByGroupId(this.groupId).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun onCourseItemClick(position: Int) {
        Log.d("lol", "onCourseItemClick: ")
        viewModelScope.launch {
            Log.d("lol", "onCourseItemClick emit: ")
            navigate.emit(
                NavigationCommand.To(
                    CourseFragmentDirections.actionGlobalCourseFragment(
                        courses.value[position].id
                    )
                )
            )
        }
    }

    fun onCourseLongItemClick(position: Int) {

    }
}