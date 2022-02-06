package com.denchic45.kts.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupViewModel @Inject constructor(
    @Named("Group ${GroupPreference.GROUP_ID}") groupId: String?,
    private val interactor: GroupInteractor
) : BaseViewModel() {

    val initTabs = MutableLiveData(2)

    val menuItemVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val title = MutableStateFlow("")

    val openUserEditor = SingleLiveData<Pair<String, String>>()

    val openGroupEditor = SingleLiveData<String>()
    private val isExistGroup: LiveData<Boolean>

    val groupId: String = groupId ?: interactor.yourGroupId
    private val isExistGroupObserver: Observer<Boolean>
    private val uiPermissions: UiPermissions
    private val groupNameByGroupId: StateFlow<String> =
        interactor.getNameByGroupId(this.groupId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

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

    fun onOptionSelect(itemId: Int) {
        when (itemId) {
            R.id.option_edit_group -> openGroupEditor.setValue(groupId)
            R.id.option_add_student -> openUserEditor.setValue(User.STUDENT to groupId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        isExistGroup.removeObserver(isExistGroupObserver)
        interactor.removeListeners()
    }

    fun onPageSelect(position: Int) {
        if (position == 0 || position == 1) {
            title.value = title.value
        }
    }

    companion object {
        const val ALLOW_EDIT_GROUP = "ALLOW_EDIT_GROUP"
        const val PAGE_GROUP_USERS = 0
        const val PAGE_GROUP_SUBJECTS = 1
    }

    init {
        viewModelScope.launch {
            groupNameByGroupId.collect { title.value = it }
        }
        isExistGroup = interactor.isExistGroup(this.groupId)
        isExistGroupObserver = Observer { exist: Boolean ->
            if (!exist) {
                finish.call()
            }
        }
        isExistGroup.observeForever(isExistGroupObserver)
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