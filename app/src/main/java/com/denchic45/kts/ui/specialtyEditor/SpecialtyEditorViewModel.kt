package com.denchic45.kts.ui.specialtyEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.SpecialtyRepository
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.LiveDataUtil
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class SpecialtyEditorViewModel @Inject constructor(
    @Named(SpecialtyEditorDialog.SPECIALTY_ID) id: String?,
    private val specialtyRepository: SpecialtyRepository,
    private val confirmInteractor: ConfirmInteractor
) : BaseViewModel() {
    val title = MutableLiveData<String>()

    val nameField = MutableLiveData<String>()

    val enablePositiveBtn = MutableLiveData(false)

    val deleteBtnVisibility = MutableLiveData(true)

    private val id = id ?: UUID.randomUUID().toString()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Specialty> =
        UIEditor(id == null) { Specialty(this.id, nameField.value ?: "") }

    private fun setupForNewItem() {
        deleteBtnVisibility.value = false
        title.value = "Создать специальность"
    }

    private fun setupForExistItem() {
        title.value = "Редактировать специальность"
        LiveDataUtil.observeOnce(specialtyRepository.find(id)) { specialty: Specialty ->
            uiEditor.oldItem = specialty
            nameField.setValue(specialty.name)
        }
    }

    private suspend fun saveChanges() {
        try {
            if (uiEditor.isNew) {
                specialtyRepository.add(uiEditor.item)
            } else {
                specialtyRepository.update(uiEditor.item)
            }
        } catch (e: Exception) {
            if (e is NetworkException) {
                showToast(R.string.error_check_network)
            } else e.printStackTrace()
        }
    }

    fun onPositiveClick() {
        uiValidator.runValidates {
            viewModelScope.launch { saveChanges() }
        }
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            openConfirmation(Pair("Удалить несколько предметов группы", "Вы точно уверены???"))
            if (confirmInteractor.receiveConfirm()) {
                try {
                    specialtyRepository.remove(uiEditor.item)
                    finish()
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                }
            }
        }
    }

    fun onNameType(name: String) {
        nameField.value = name
        enablePositiveBtn.postValue(uiValidator.runValidates())
    }

    init {
        uiValidator = UIValidator.of(
            Validation(Rule({ !TextUtils.isEmpty(uiEditor.item.name) }, "Нет названия!")),
            Validation(Rule { uiEditor.hasBeenChanged() })
        )
        if (uiEditor.isNew) setupForNewItem() else setupForExistItem()
    }
}