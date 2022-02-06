package com.denchic45.kts.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.rx.AsyncTransformer
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ProfileViewModel @Inject constructor(
    @Named(ProfileFragment.USER_ID) id: String,
    private val interactor: ProfileInteractor
) : BaseViewModel() {
    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()
    val showAvatar = MutableLiveData<String>()
    val showFullName = MutableLiveData<String>()
    val showRole = MutableLiveData<Int>()
    val showGroupInfo = MutableLiveData<String>()
    val showPhoneNum = MutableLiveData<String>()
    val showEmail = MutableLiveData<String?>()
    val infoVisibility = MutableLiveData<Boolean>()
    val groupInfoVisibility = MutableLiveData(false)
    val openGroup = SingleLiveData<String>()
    val openFullImage = SingleLiveData<String>()
    val openGallery = SingleLiveData<Void>()
    private val uiPermissions: UiPermissions
    private val compositeDisposable = CompositeDisposable()
    private var userOfProfile: User? = null
    private var group: Group? = null
    fun onCreateOptions() {}
    private fun mapRoleToNameRoleId(@User.Role role: String): Int {
        return when (role) {
            User.STUDENT -> R.string.role_student
            User.CLASS_MONITOR -> R.string.role_classMonitor
            User.DEPUTY_MONITOR -> R.string.role_deputyMonitor
            User.TEACHER -> R.string.role_teacher
            User.HEAD_TEACHER -> R.string.role_headTeacher
            else -> throw IllegalArgumentException(role)
        }
    }

    fun onGroupInfoClick() {
        openGroup.value = group!!.id
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
        compositeDisposable.clear()
    }

    fun onAvatarClick() {
        if (!userOfProfile!!.generatedAvatar) openFullImage.value = showAvatar.value
    }

    fun onOptionClick(id: Int) {
        when (id) {
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
        compositeDisposable.addAll(interactor.find(id)
            .compose(AsyncTransformer())
            .subscribe { user: User ->
                userOfProfile = user
                showAvatar.value = user.photoUrl
                showFullName.value = user.fullName
                showRole.value = mapRoleToNameRoleId(user.role)
                showPhoneNum.value = user.phoneNum
                showEmail.value = user.email
                if (user.isStudent) {
                    compositeDisposable.add(interactor.findGroupByStudent(user)
                        .compose(AsyncTransformer())
                        .subscribe { group: Group ->
                            groupInfoVisibility.value = true
                            this.group = group
                            showGroupInfo.setValue("Участник группы: " + group.name)
                        })
                } else if (user.isTeacher) {
                    compositeDisposable.add(interactor.findGroupByCurator(user)
                        .compose(AsyncTransformer())
                        .subscribe { group: Group? ->
                            if (group == null) return@subscribe
                            groupInfoVisibility.value = true
                            this.group = group
                            showGroupInfo.setValue("Куратор группы: " + group.name)
                        })
                }
                if (interactor.findThisUser().id != userOfProfile!!.id) {
                    optionVisibility.value = Pair(R.id.menu_select_avatar, false)
                }
            })
        uiPermissions = UiPermissions(interactor.findThisUser())
        uiPermissions.putPermissions(
            Permission(PERMISSION_USER_NFO, { hasAdminPerms() }, { isTeacher }, { this.id == id })
        )
        infoVisibility.value = uiPermissions.isAllowed(PERMISSION_USER_NFO)
    }
}