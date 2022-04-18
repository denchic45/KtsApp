package com.denchic45.kts.ui.group.users

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.GroupMember
import com.denchic45.kts.data.model.domain.GroupMembers
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.ui.Header
import com.denchic45.kts.data.model.ui.UserItem
import com.denchic45.kts.domain.usecase.FindGroupMembersUseCase
import com.denchic45.kts.domain.usecase.FindSelfUserUseCase
import com.denchic45.kts.domain.usecase.RemoveStudentUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.teacherChooser.TeacherChooserInteractor
import com.denchic45.kts.ui.userEditor.UserEditorFragment
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupMembersViewModel @Inject constructor(
    private val interactor: GroupUsersInteractor,
    findSelfUserUseCase: FindSelfUserUseCase,
    findGroupMembersUseCase: FindGroupMembersUseCase,
    private val removeStudentUseCase: RemoveStudentUseCase,
    private val teacherChooserInteractor: TeacherChooserInteractor,
    @Named("options_user") private val userOptions: List<ListItem>,
    @Named(GroupMembersFragment.GROUP_ID) groupId: String?
) : BaseViewModel() {

    val showUserOptions = SingleLiveData<Pair<Int, List<ListItem>>>()

    val openUserEditor = SingleLiveData<Map<String, String>>()

    private val uiPermissions: UiPermissions = UiPermissions(findSelfUserUseCase())

    private var groupId: String = groupId ?: interactor.yourGroupId

    private val _members: SharedFlow<GroupMembers> =
        findGroupMembersUseCase(this.groupId).shareIn(viewModelScope, SharingStarted.Lazily)

    val members: StateFlow<List<DomainModel>> =
        _members.map { groupMembers ->
            mutableListOf<DomainModel>().apply {
                add(Header("Куратор"))
                add(groupMembers.curator.toUserItem(groupMembers))
                add(Header("Студенты"))
                addAll(
                    groupMembers.students.map { student ->
                        student.toUserItem(groupMembers)
                    }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private lateinit var selectedUserId: String

    init {
        uiPermissions.putPermissions(
            Permission(ALLOW_EDIT_USERS, { isTeacher }, { hasAdminPerms() })
        )

    }

    fun onUserItemLongClick(position: Int) {
        if (uiPermissions.isAllowed(ALLOW_EDIT_USERS)) {
            viewModelScope.launch {
                selectedUserId = members.value[position].id
                showUserOptions.value = position to userOptions
            }
        }
    }

    fun onUserItemClick(position: Int) {
        navigateTo(MobileNavigationDirections.actionGlobalProfileFragment(members.value[position].id))
    }

    fun onOptionUserClick(optionId: String) {
        when (optionId) {
            OPTION_SHOW_PROFILE -> {
            }
            OPTION_EDIT_USER -> {
                val args: MutableMap<String, String> = HashMap()
//                args[UserEditorFragment.USER_ROLE] = selectedUser.role
                args[UserEditorFragment.USER_ID] = selectedUserId
//                args[UserEditorFragment.USER_GROUP_ID] = selectedUser.groupId!!
                openUserEditor.setValue(args)
            }
            OPTION_DELETE_USER -> viewModelScope.launch {
                try {
                    removeStudentUseCase(selectedUserId)
//                    interactor.removeStudent(selectedUser)
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                }
            }
            OPTION_CHANGE_CURATOR -> {
                viewModelScope.launch {
                    navigateTo(MobileNavigationDirections.actionGlobalTeacherChooserFragment())
                    teacherChooserInteractor.receiveSelectTeacher().apply {
                        try {
                            interactor.updateGroupCurator(this@GroupMembersViewModel.groupId, this)
                        } catch (e: Exception) {
                            if (e is NetworkException) {
                                showToast(R.string.error_check_network)
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    companion object {
        const val ALLOW_EDIT_USERS = "EDIT_USERS"
        const val OPTION_SHOW_PROFILE = "OPTION_SHOW_PROFILE"
        const val OPTION_EDIT_USER = "OPTION_EDIT_USER"
        const val OPTION_DELETE_USER = "OPTION_DELETE_USER"
        const val OPTION_CHANGE_CURATOR = "OPTION_CHANGE_CURATOR"
    }
}

private fun GroupMember.toUserItem(groupMembers: GroupMembers): UserItem {
    return UserItem(
        id = id,
        title = fullName,
        photoUrl = photoUrl,
        subtitle = if (groupMembers.isHeadman(this)) "Староста" else null
    )
}
