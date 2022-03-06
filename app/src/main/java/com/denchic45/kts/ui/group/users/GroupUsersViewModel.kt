package com.denchic45.kts.ui.group.users

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.adapter.ItemAdapter
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupUsersViewModel @Inject constructor(
    private val interactor: GroupUsersInteractor,
    @Named("options_user") private val userOptions: List<ListItem>
) : BaseViewModel() {
    @JvmField
    val showUserOptions = SingleLiveData<Pair<Int, List<ListItem>>>()

    @JvmField
    val openProfile = SingleLiveData<String>()

    @JvmField
    val openUserEditor = SingleLiveData<Map<String, String>>()

    @JvmField
    val openChoiceOfCurator = SingleLiveData<Void>()

    private val uiPermissions: UiPermissions = UiPermissions(interactor.findThisUser())

    lateinit var users: StateFlow<List<DomainModel?>>

    @JvmField
    @Inject
    var choiceOfCuratorInteractor: ChoiceOfCuratorInteractor? = null
    private var groupId: String = ""
    private lateinit var selectedUser: User
    private var students: List<User> = emptyList()
    fun onGroupIdReceived(groupId: String?) {
        var groupId = groupId
        if (groupId == null) {
            groupId = interactor.yourGroupId
            this.groupId = groupId
        }

        users = interactor.getUsersByGroupId(groupId)
            .combine(interactor.getCurator(groupId)) { students, curator ->
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
    }

    fun onUserItemLongClick(position: Int) {
        if (uiPermissions.isAllowed(ALLOW_EDIT_USERS)) {
            val user = users.value[position] as User
            showUserOptions.value = position to userOptions
            selectedUser = user
        }
    }

    fun onUserItemClick(position: Int) {
        openProfile.value = users.value[position]!!.id
    }

    fun onOptionUserClick(optionId: String) {
        when (optionId) {
            OPTION_SHOW_PROFILE -> {
            }
            OPTION_EDIT_USER -> {
                val args: MutableMap<String, String> = HashMap()
                args[UserEditorActivity.USER_ROLE] = selectedUser.role
                args[UserEditorActivity.USER_ID] = selectedUser.id
                args[UserEditorActivity.USER_GROUP_ID] = selectedUser.groupId!!
                openUserEditor.setValue(args)
            }
            OPTION_DELETE_USER -> viewModelScope.launch {
                try {
                    interactor.removeStudent(selectedUser)
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showMessageRes.value = R.string.error_check_network
                    }
                }
            }
            OPTION_CHANGE_CURATOR -> {
                viewModelScope.launch {
                    openChoiceOfCurator.call()
                    choiceOfCuratorInteractor!!.awaitSelectTeacher().apply {
                        try {
                            interactor.updateGroupCurator(this@GroupUsersViewModel.groupId, this)
                        } catch (e: Exception) {
                            if (e is NetworkException) {
                                showMessageRes.value = R.string.error_check_network
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

    init {
        uiPermissions.putPermissions(
            Permission(ALLOW_EDIT_USERS, { isTeacher }, { hasAdminPerms() })
        )
    }
}