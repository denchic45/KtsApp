package com.denchic45.kts.ui.course.content

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.domain.usecase.FindSelfUserUseCase
import com.denchic45.kts.domain.usecase.IsCourseTeacherUseCase
import com.denchic45.kts.domain.usecase.RemoveCourseContentUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.course.taskInfo.TaskInfoViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ContentViewModel @Inject constructor(
    findSelfUserUseCase: FindSelfUserUseCase,
    @Named(ContentFragment.COURSE_ID)
    val courseId: String,
    @Named(ContentFragment.TASK_ID)
    val taskId: String,
    private val confirmInteractor: ConfirmInteractor,
    private val removeCourseContentUseCase: RemoveCourseContentUseCase,
    private val isCourseTeacherUseCase: IsCourseTeacherUseCase
) : BaseViewModel() {
    val tabNames = listOf("Инфо", "Ответы")

    val openTaskEditor = SingleLiveData<Pair<String, String>>()

    val submissionVisibility = MutableSharedFlow<Boolean>(replay = 1)

    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()

    private val selfUser: User = findSelfUserUseCase()
    private val uiPermissions: UiPermissions = UiPermissions(selfUser)

    companion object {
        const val ALLOW_SEE_SUBMISSION = "ALLOW_SEE_SUBMISSION"
    }

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_task_edit -> openTaskEditor.value = taskId to courseId
            R.id.options_task_delete -> {
                openConfirmation.value = "" to ""
                viewModelScope.launch {
                    if (confirmInteractor.awaitConfirm()) {
                        removeCourseContentUseCase(taskId)
                        finish.call()
                    }
                }
            }
        }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        viewModelScope.launch {
            val courseTeacherUseCase = isCourseTeacherUseCase(selfUser.id, courseId)
            uiPermissions.putPermissions(
                Permission(
                    (ALLOW_SEE_SUBMISSION),
                    { hasAdminPerms() },
                    { courseTeacherUseCase })
            )

            uiPermissions.putPermissions(
                Permission(
                    (TaskInfoViewModel.ALLOW_EDIT_TASK),
                    { hasAdminPerms() },
                    { courseTeacherUseCase })
            )

            submissionVisibility.emit(uiPermissions.isAllowed(ALLOW_SEE_SUBMISSION))

            if (uiPermissions.isAllowed(TaskInfoViewModel.ALLOW_EDIT_TASK)) {
                optionVisibility.value = R.id.option_task_edit to true
                optionVisibility.value = R.id.options_task_delete to true
            }
        }
    }
}