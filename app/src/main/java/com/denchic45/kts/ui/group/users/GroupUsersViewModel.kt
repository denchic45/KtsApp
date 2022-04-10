package com.denchic45.kts.ui.group.users

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.adapter.ItemAdapter
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

class GroupUsersViewModel @Inject constructor(
    private val interactor: GroupUsersInteractor,
    private val teacherChooserInteractor: TeacherChooserInteractor,
    @Named("options_user") private val userOptions: List<ListItem>,
    @Named(GroupUsersFragment.GROUP_ID) groupId: String?
) : BaseViewModel() {

    val showUserOptions = SingleLiveData<Pair<Int, List<ListItem>>>()

    val openUserEditor = SingleLiveData<Map<String, String>>()

    private val uiPermissions: UiPermissions = UiPermissions(interactor.findThisUser())

    private var groupId: String = groupId ?: interactor.yourGroupId

    val users: StateFlow<List<DomainModel>> = interactor.getUsersByGroupId(this.groupId)
        .combine(interactor.getCurator(this.groupId).filterNotNull()) { students, curator ->
            this.students = students
            val userList: MutableList<DomainModel> = ArrayList(students)
            userList.add(0, curator)
            userList.add(
                0,
                ListItem(id = "", title = "Куратор", type = ItemAdapter.TYPE_HEADER)
            )
            userList.add(
                2,
                ListItem(id = "", title = "Студенты", type = ItemAdapter.TYPE_HEADER)
            )
            userList
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private lateinit var selectedUser: User
    private var students: List<User> = emptyList()

    init {
        uiPermissions.putPermissions(
            Permission(ALLOW_EDIT_USERS, { isTeacher }, { hasAdminPerms() })
        )

    }

    fun onUserItemLongClick(position: Int) {
        if (uiPermissions.isAllowed(ALLOW_EDIT_USERS)) {
            val user = users.value[position] as User
            showUserOptions.value = position to userOptions
            selectedUser = user
        }
    }

    fun onUserItemClick(position: Int) {
        navigateTo(MobileNavigationDirections.actionGlobalProfileFragment(users.value[position].id))
    }

    fun onOptionUserClick(optionId: String) {
        when (optionId) {
            OPTION_SHOW_PROFILE -> {
            }
            OPTION_EDIT_USER -> {
                val args: MutableMap<String, String> = HashMap()
                args[UserEditorFragment.USER_ROLE] = selectedUser.role
                args[UserEditorFragment.USER_ID] = selectedUser.id
                args[UserEditorFragment.USER_GROUP_ID] = selectedUser.groupId!!
                openUserEditor.setValue(args)
            }
            OPTION_DELETE_USER -> viewModelScope.launch {
                try {
                    interactor.removeStudent(selectedUser)
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
                            interactor.updateGroupCurator(this@GroupUsersViewModel.groupId, this)
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