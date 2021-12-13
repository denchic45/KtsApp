package com.denchic45.kts.ui.group.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.denchic45.CombinedLiveData
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
import com.denchic45.kts.uipermissions.UIPermissions
import com.denchic45.kts.utils.NetworkException
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import java.util.function.Predicate
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList

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

    private val uiPermissions: UIPermissions = UIPermissions(interactor.findThisUser())

    @JvmField
    var users: LiveData<List<DomainModel?>>? = null

    @JvmField
    @Inject
    var choiceOfCuratorInteractor: ChoiceOfCuratorInteractor? = null
    private var groupUuid: String? = null
    private var selectedUser: User? = null
    private var subscribeChoiceOfCurator: Disposable? = null
    private var students: List<User> = emptyList()
    fun onGroupUuidReceived(groupUuid: String?) {
        var groupUuid = groupUuid
        if (groupUuid == null) {
            groupUuid = interactor.yourGroupUuid
            this.groupUuid = groupUuid
        }
        val groupUsers = CombinedLiveData(
            interactor.getUsersByGroupUuid(groupUuid),
            interactor.getCurator(groupUuid)
        )
        users = Transformations.map(groupUsers) { studentsWithCurator: Pair<List<User>, User> ->
            students = studentsWithCurator.first
            val userList: MutableList<DomainModel> = ArrayList(students)
            userList.add(0, studentsWithCurator.second)
            userList.add(0, ListItem(uuid = "", title = "Куратор", type = ItemAdapter.TYPE_HEADER))
            userList.add(2, ListItem(uuid = "", title = "Студенты", type = ItemAdapter.TYPE_HEADER))
            userList
        }
    }

    fun onUserItemLongClick(position: Int) {
        if (uiPermissions.isAllowed(ALLOW_EDIT_USERS)) {
            val user = users!!.value!![position] as User
            showUserOptions.value = position to userOptions
            selectedUser = user
        }
    }

    fun onUserItemClick(position: Int) {
        openProfile.value = users!!.value!![position]!!.uuid
    }

    fun onOptionUserClick(uuidOption: String) {
        when (uuidOption) {
            OPTION_SHOW_PROFILE -> {
            }
            OPTION_EDIT_USER -> {
                val args: MutableMap<String, String> = HashMap()
                args[UserEditorActivity.USER_ROLE] = selectedUser!!.role
                args[UserEditorActivity.USER_UUID] = selectedUser!!.uuid
                args[UserEditorActivity.USER_GROUP_UUID] = selectedUser!!.groupUuid!!
                openUserEditor.setValue(args)
            }
            OPTION_DELETE_USER -> interactor.removeStudent(selectedUser)
                .subscribe(
                    {}
                ) { throwable: Throwable? ->
                    if (throwable is NetworkException) {
                        showMessageRes.value = R.string.error_check_network
                    }
                }
            OPTION_CHANGE_CURATOR -> {
                openChoiceOfCurator.call()
                subscribeChoiceOfCurator = choiceOfCuratorInteractor!!.observeSelectedCurator()
                    .subscribe { teacher: User ->
                        interactor.updateGroupCurator(groupUuid, teacher)
                            .subscribe(
                                {}
                            ) { throwable: Throwable? ->
                                if (throwable is NetworkException) {
                                    showMessageRes.value = R.string.error_check_network
                                }
                            }
                        subscribeChoiceOfCurator!!.dispose()
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
        uiPermissions.addPermissions(
            Permission(
                ALLOW_EDIT_USERS,
                Predicate { user: User -> user.isTeacher || user.admin })
        )
    }
}