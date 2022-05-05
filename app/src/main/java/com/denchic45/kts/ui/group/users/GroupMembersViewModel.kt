package com.denchic45.kts.ui.group.users

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.GroupMember
import com.denchic45.kts.data.model.domain.GroupMembers
import com.denchic45.kts.data.model.domain.OptionItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.ui.Header
import com.denchic45.kts.data.model.ui.UiText
import com.denchic45.kts.data.model.ui.UserItem
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.teacherChooser.TeacherChooserInteractor
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
    private val setHeadmanUseCase: SetHeadmanUseCase,
    private val removeHeadmanUseCase: RemoveHeadmanUseCase,
    private val teacherChooserInteractor: TeacherChooserInteractor,
    @Named(GroupMembersFragment.GROUP_ID) groupId: String?
) : BaseViewModel() {

    val showUserOptions = SingleLiveData<Pair<Int, List<OptionItem>>>()

    private val uiPermissions: UiPermissions = UiPermissions(findSelfUserUseCase())

    private var groupId: String = groupId ?: interactor.yourGroupId

    private val _members: SharedFlow<GroupMembers> =
        findGroupMembersUseCase(this.groupId).shareIn(
            viewModelScope,
            SharingStarted.Lazily,
            replay = 1
        )

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
        viewModelScope.launch {
            selectedUserId = members.value[position].id
            if (uiPermissions.isAllowed(ALLOW_EDIT_USERS)
                && _members.first().students.any { it.id == selectedUserId }
            ) {
                showUserOptions.value = position to buildUserOptions()
            }
        }
    }

    private suspend fun buildUserOptions(): List<OptionItem> {
        return buildList {
            with(_members.first()) {
                add(
                    if (headmanId == selectedUserId) {
                        OptionItem(
                            id = "OPTION_REMOVE_HEADMAN",
                            title = UiText.IdText(R.string.option_remove_headman)
                        )
                    } else {
                        OptionItem(
                            id = "OPTION_SET_HEADMAN",
                            title = UiText.IdText(R.string.option_set_headman)
                        )
                    }
                )
                add(
                    OptionItem(
                        id = "OPTION_EDIT_USER",
                        title = UiText.IdText(R.string.option_edit_user)
                    )
                )
                add(
                    OptionItem(
                        id = "OPTION_REMOVE_USER",
                        title = UiText.IdText(R.string.option_remove_user)
                    )
                )
            }
        }
    }

    fun onUserItemClick(position: Int) {
        navigateTo(MobileNavigationDirections.actionGlobalProfileFragment(members.value[position].id))
    }

    fun onOptionUserClick(optionId: String) {
        viewModelScope.launch {
            when (optionId) {
                OPTION_SHOW_PROFILE -> {
                }
                OPTION_EDIT_USER -> {
                    navigateTo(
                        MobileNavigationDirections.actionGlobalUserEditorFragment(
                            userId = selectedUserId,
                            role = (
                                    if (_members.first().students.any { it.id == selectedUserId })
                                        User.Role.STUDENT else User.Role.TEACHER
                                    ).toString(),
                            groupId = null
                        )
                    )
                }
                OPTION_REMOVE_USER -> {
                    try {
                        removeStudentUseCase(selectedUserId)
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                            showToast(R.string.error_check_network)
                        }
                    }
                }
                OPTION_SET_HEADMAN -> {
                    setHeadmanUseCase(selectedUserId, groupId)
                }
                OPTION_REMOVE_HEADMAN -> {
                    removeHeadmanUseCase(groupId)
                }
                OPTION_CHANGE_CURATOR -> {
                    navigateTo(MobileNavigationDirections.actionGlobalTeacherChooserFragment())
                    teacherChooserInteractor.receiveSelectTeacher().apply {
                        try {
                            interactor.updateGroupCurator(
                                this@GroupMembersViewModel.groupId,
                                this
                            )
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
        const val OPTION_REMOVE_USER = "OPTION_REMOVE_USER"
        const val OPTION_CHANGE_CURATOR = "OPTION_CHANGE_CURATOR"
        const val OPTION_SET_HEADMAN = "OPTION_SET_HEADMAN"
        const val OPTION_REMOVE_HEADMAN = "OPTION_REMOVE_HEADMAN"
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
