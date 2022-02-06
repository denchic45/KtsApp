package com.denchic45.kts.ui.group.courses

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Named

class GroupCoursesViewModel @Inject constructor(
    interactor: GroupCoursesInteractor,
    @Named("GroupCourses ${GroupPreference.GROUP_ID}") groupId: String?
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

    }

    fun onCourseLongItemClick(position: Int) {

    }
}