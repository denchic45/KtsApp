package com.denchic45.kts.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.domain.model.Group
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ProfileViewModel @Inject constructor(
    @Named(ProfileFragment.USER_ID) id: String,
    private val interactor: ProfileInteractor
) : BaseViewModel() {
    val showAvatar = MutableLiveData<String>()
    val showFullName = MutableLiveData<String>()
    val showRole = MutableLiveData<Int>()
    val showGroupInfo = MutableLiveData<String>()
    val showEmail = MutableLiveData<String?>()
    val infoVisibility = MutableLiveData<Boolean>()
    val groupInfoVisibility = MutableLiveData(false)
    val openFullImage = SingleLiveData<String>()
    val openGallery = SingleLiveData<Void>()
    private val uiPermissions: UiPermissions
    private var userOfProfile: User? = null
    private var group: Group? = null

    private fun mapRoleToNameRoleId(role: User.Role): Int {
        return when (role) {
            User.Role.STUDENT -> R.string.role_student
            User.Role.TEACHER -> R.string.role_teacher
            User.Role.HEAD_TEACHER -> R.string.role_headTeacher
        }
    }

    fun onGroupInfoClick() {
        navigateTo(MobileNavigationDirections.actionGlobalMenuGroup(group!!.id))
    }

    init {
        toolbarTitle = ""
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onAvatarClick() {
        if (!userOfProfile!!.generatedAvatar) openFullImage.value = showAvatar.value
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.menu_select_avatar -> openGallery.call()
        }
    }

    fun onImageLoad(imageBytes: ByteArray?) {
        viewModelScope.launch {
            interactor.updateAvatar(userOfProfile!!, imageBytes!!)
        }
    }

    companion object {
        private const val PERMISSION_USER_NFO = "PERMISSION_USER_NFO"
    }

    init {
        viewModelScope.launch {
            interactor.observe(id).collect { user ->
                user?.let {
                    userOfProfile = user
                    showAvatar.value = user.photoUrl
                    showFullName.value = user.fullName
                    showRole.value = mapRoleToNameRoleId(user.role)
                    showEmail.value = user.email
                    if (user.isStudent) {
                        viewModelScope.launch {
                            interactor.findGroupByStudent(user).collect {
                                groupInfoVisibility.value = true
                                this@ProfileViewModel.group = it
                                showGroupInfo.setValue("???????????????? ????????????: " + it.name)
                            }
                        }
                    } else if (user.isTeacher) {
                        viewModelScope.launch {
                            interactor.findGroupByCurator(user).collect {
                                if (group == null) return@collect
                                groupInfoVisibility.value = true
                                this@ProfileViewModel.group = it
                                showGroupInfo.setValue("?????????????? ????????????: " + it.name)
                            }
                        }
                    }
                    if (interactor.findThisUser().id != userOfProfile!!.id) {
                        setMenuItemVisible(R.id.menu_select_avatar to false)
                    }
                }
            }
        }

        uiPermissions = UiPermissions(interactor.findThisUser())
        uiPermissions.putPermissions(
            Permission(PERMISSION_USER_NFO, { hasAdminPerms() }, { isTeacher }, { this.id == id })
        )
        infoVisibility.value = uiPermissions.isAllowed(PERMISSION_USER_NFO)
    }
}