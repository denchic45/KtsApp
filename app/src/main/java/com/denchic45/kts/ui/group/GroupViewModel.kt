package com.denchic45.kts.ui.group

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupViewModel @Inject constructor(
    @Named(GroupFragment.GROUP_ID) groupId: String?,
    private val interactor: GroupInteractor
) : BaseViewModel() {

    val initTabs = MutableLiveData(2)

    val menuItemVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val openGroupEditor = SingleLiveData<String>()

    val groupId: String = groupId ?: interactor.yourGroupId

    val isExist = interactor.isExistGroup(this@GroupViewModel.groupId).onEach { exist ->
        Log.d("lol", "exist isExistGroup: $exist")
        if (!exist) {
            finish()
        }
    }
    private val uiPermissions: UiPermissions

    fun onPrepareOptions(currentItem: Int) {
        menuItemVisibility.value = Pair(
            R.id.option_edit_group,
            uiPermissions.isAllowed(ALLOW_EDIT_GROUP)
        )
        when (currentItem) {
            PAGE_GROUP_USERS -> menuItemVisibility.setValue(
                Pair(
                    R.id.option_add_student, uiPermissions.isAllowed(
                        ALLOW_EDIT_GROUP
                    )
                )
            )
            PAGE_GROUP_SUBJECTS -> menuItemVisibility.setValue(Pair(R.id.option_add_student, false))
        }
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_edit_group -> openGroupEditor.setValue(groupId)
            R.id.option_add_student -> {
                navigateTo(
                    MobileNavigationDirections.actionGlobalUserEditorFragment(
                        userId = null,
                        role = User.Role.STUDENT.toString(),
                        groupId = groupId
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onPageSelect(position: Int) {
        if (position == 0 || position == 1) {
            toolbarTitle = toolbarTitle
        }
    }

    companion object {
        const val ALLOW_EDIT_GROUP = "ALLOW_EDIT_GROUP"
        const val PAGE_GROUP_USERS = 0
        const val PAGE_GROUP_SUBJECTS = 1
    }

    init {
        viewModelScope.launch {
            interactor.getNameByGroupId(this@GroupViewModel.groupId).collect {
                toolbarTitle = it
            }
        }


        uiPermissions = UiPermissions(interactor.findThisUser())
        uiPermissions.putPermissions(
            Permission(
                ALLOW_EDIT_GROUP, { hasAdminPerms() }, { curatorFor(this@GroupViewModel.groupId) }
            )
        )
        if (uiPermissions.isAllowed(ALLOW_EDIT_GROUP)) {
            initTabs.setValue(3)
        } else {
            initTabs.setValue(2)
        }
    }
}