package com.denchic45.kts.ui.group.courses

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.group.GroupViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UIPermissions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.function.Predicate
import javax.inject.Inject
import javax.inject.Named

class GroupCoursesViewModel @Inject constructor(
    interactor: GroupCoursesInteractor,
    @Named("GroupCourses ${GroupPreference.GROUP_ID}") groupId: String?
) : BaseViewModel() {
    val clearItemsSelection = SingleLiveData<Set<Int>>()
    val selectItem = MutableLiveData<Pair<Int, Boolean>>()
    private val groupId: String = groupId ?: interactor.yourGroupId
    private val uiPermissions: UIPermissions = UIPermissions(interactor.findThisUser())
    var openCourseEditorDialog = SingleLiveData<Course>()
    var courses: StateFlow<List<Course>> =
        interactor.findCoursesByGroupId(this.groupId).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun onCourseItemClick(position: Int) {
        if (uiPermissions.isAllowed(GroupViewModel.ALLOW_EDIT_GROUP)) openCourseEditorDialog.value =
            courses.value[position]
    }

    fun onCourseLongItemClick(position: Int) {

    }

    init {
        uiPermissions.addPermissions(
            Permission(
                GroupViewModel.ALLOW_EDIT_GROUP,
                Predicate { (_, _, _, _, _, role, _, _, _, _, _, _, admin) -> role == User.HEAD_TEACHER || admin })
        )
    }
}