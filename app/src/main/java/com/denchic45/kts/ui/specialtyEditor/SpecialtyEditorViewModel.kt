package com.denchic45.kts.ui.specialtyEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.SpecialtyRepository
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.LiveDataUtil
import com.denchic45.kts.utils.NetworkException
import io.reactivex.rxjava3.core.Completable
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class SpecialtyEditorViewModel @Inject constructor(
    @Named(SpecialtyEditorDialog.SPECIALTY_ID) uuid: String?,
    private val specialtyRepository: SpecialtyRepository
) :
    BaseViewModel() {
    @JvmField
    val title = MutableLiveData<String>()

    @JvmField
    val nameField = MutableLiveData<String>()

    @JvmField
    val enablePositiveBtn = MutableLiveData(false)

    @JvmField
    val deleteBtnVisibility = MutableLiveData(true)

    private val uuid = uuid ?: UUID.randomUUID().toString()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Specialty> =
        UIEditor(uuid == null) { Specialty(this.uuid, nameField.value ?: "") }

    private fun setupForNewItem() {
        deleteBtnVisibility.value = false
        title.value = "Создать специальность"
    }

    private fun setupForExistItem() {
        title.value = "Редактировать специальность"
        LiveDataUtil.observeOnce(specialtyRepository.find(uuid)) { specialty: Specialty ->
            uiEditor.oldItem = specialty
            nameField.setValue(specialty.name)
        }
    }

    private fun saveChanges() {
        val saveSubjectCompletable: Completable = if (uiEditor.isNew) {
            specialtyRepository.add(uiEditor.item)
        } else {
            specialtyRepository.update(uiEditor.item)
        }
        saveSubjectCompletable.subscribe({ finish.call() }) { throwable: Throwable? ->
            if (throwable is NetworkException) {
                showMessageRes.value = R.string.error_check_network
            }
        }
    }

    fun onPositiveClick() {
        uiValidator.runValidates { saveChanges() }
    }

    fun onDeleteClick() {
        openConfirmation.value = Pair("Удалить несколько предметов группы", "Вы точно уверены???")
        RxBusConfirm.getInstance()
            .event
            .subscribe { confirm: Boolean ->
                if (confirm) {
                    specialtyRepository.remove(uiEditor.item)
                        .subscribe({ finish.call() }) { throwable: Throwable? ->
                            if (throwable is NetworkException) {
                                showMessageRes.value = R.string.error_check_network
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