package com.denchic45.kts.ui.profile.fullAvatar

import androidx.lifecycle.viewModelScope

import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.avatar.FullImageActivity
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class FullAvatarViewModel @Inject constructor(
    @Named(FullImageActivity.IMAGE_URL) private val photoUrl: String,
    private val interactor: FullAvatarInteractor
) : BaseViewModel() {
    @JvmField
    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()

    private var thisUser: User? = null
    override fun onCreateOptions() {
        super.onCreateOptions()
        thisUser = interactor.findThisUser()
        if (photoUrl != thisUser!!.photoUrl) {
            optionVisibility.value = Pair(R.id.menu_delete_avatar, false)
        }
    }

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            android.R.id.home -> finish.call()
            R.id.menu_delete_avatar -> {
                viewModelScope.launch {
                    try {
                        interactor.removeUserAvatar(thisUser!!)
                        finish.call()
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                            showMessageRes.setValue(R.string.error_check_network)
                        } else {
                            showMessage.setValue("Произошла ошибка")
                        }
                    }
                }
            }
        }
    }

}