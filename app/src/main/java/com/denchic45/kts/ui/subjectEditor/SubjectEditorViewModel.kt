package com.denchic45.kts.ui.subjectEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.data.repository.SameSubjectIconException
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.iconPicker.IconPickerInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.util.Colors
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.UUIDS
import kotlinx.coroutines.launch
import java.util.stream.IntStream
import javax.inject.Inject
import javax.inject.Named

class SubjectEditorViewModel @Inject constructor(
    @Named(SubjectEditorDialog.SUBJECT_ID) subjectId: String?,
    private val interactor: SubjectEditorInteractor,
    private val iconPickerInteractor: IconPickerInteractor,
    private val confirmInteractor: ConfirmInteractor
) : BaseViewModel() {

    val title = MutableLiveData<String>()

    val icon = MutableLiveData<String>()

    val colorIcon = MutableLiveData(R.color.blue)

    val nameField = MutableLiveData<String>()

    val deleteBtnVisibility = MutableLiveData(true)

    val enablePositiveBtn = MutableLiveData(false)

    val currentSelectedColorPosition = MutableLiveData(0)

    val openIconPicker = SingleLiveData<Void>()

    val showColors = MutableLiveData<Pair<List<Int>, Int>>()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Subject>
    private val id: String = subjectId ?: UUIDS.createShort()
    private var colorName = ""

    fun onColorSelect(position: Int) {
        val colorId = Colors.ids[position]
        colorName = Colors.colorNameOfId[colorId]!!
        colorIcon.value = colorId
        enablePositiveBtn.postValue(uiValidator.runValidates())
    }

    private suspend fun saveChanges() {
        try {
            if (uiEditor.isNew) {
                interactor.add(uiEditor.item)
            } else {
                interactor.update(uiEditor.item)
            }
            finish()
        } catch (e: Exception) {
            when (e) {
                is NetworkException -> {
                    showToast(R.string.error_check_network)
                }
                is SameSubjectIconException -> {
                    showToast("?????????? ???????????? ?????? ????????????????????????!")
                }
                else -> e.printStackTrace()
            }

        }
    }

    private fun setupForNewItem() {
        title.value = "?????????????? ??????????????"
        colorName = "blue"
        deleteBtnVisibility.value = false
        showColors.value = Pair(Colors.ids, 0)
    }

    private fun setupForExistItem() {
        title.value = "?????????????????????????? ??????????????"
        viewModelScope.launch {
            interactor.find(id).collect { subject: Subject? ->
                subject?.let {
                    uiEditor.oldItem = subject
                    nameField.value = subject.name
                    colorIcon.value = Colors.colorIdOfName[subject.colorName]

                    currentSelectedColorPosition.value = IntStream.range(0, Colors.names.size)
                        .filter { value: Int -> Colors.names[value] == subject.colorName }
                        .findFirst()
                        .orElse(-1)
                    icon.value = subject.iconUrl

                    Colors.names
                        .firstOrNull { name -> name == subject.colorName }
                        ?.let { name ->
                            colorIcon.value = Colors.colorIdOfName[name]
                            showColors.setValue(Pair(Colors.ids, Colors.names.indexOf(name)))
                        }

                    colorName = subject.colorName
                } ?: run {
                    finish()
                }
            }
        }
    }

//    private fun findColorId(colorName: String): Int {
//        return colorsNames.firstOrNull { name -> name == colorName }
//            ?.let { it.value }
//            ?: R.color.blue
//    }

    fun onNameType(name: String) {
        nameField.postValue(name)
        enablePositiveBtn.postValue(uiValidator.runValidates())
    }

    fun onPositiveClick() {
        uiValidator.runValidates {
            viewModelScope.launch { saveChanges() }
        }
    }

    fun onDeleteClick() {
        openConfirmation(
            "?????????????? ??????????????" to
                    "???????????? ?? ?????????????????? ???????????????? ?????????????????????????? ?????? ?????????? ?????? ?????????????????????? ????????????????????????????"
        )
        viewModelScope.launch {
            if (confirmInteractor.receiveConfirm()) {
                try {
                    interactor.remove(uiEditor.oldItem!!)
                    finish()
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                }
            }
        }
    }

    fun onIconClick() {
        viewModelScope.launch {
            openIconPicker.call()
            iconPickerInteractor.observeSelectedIcon().apply {
                icon.value = this
                enablePositiveBtn.postValue(uiValidator.runValidates())
            }
        }
    }

    init {
        uiEditor = UIEditor(subjectId == null) {
            Subject(
                id,
                nameField.value ?: "",
                icon.value ?: "",
                colorName
            )
        }
        uiValidator = UIValidator.of(
            Validation(Rule { uiEditor.hasBeenChanged() }),
            Validation(Rule({ !nameField.value.isNullOrEmpty() }, "?????? ????????????????")),
            Validation(Rule({ !icon.value.isNullOrEmpty() }, "?????? ????????????"))
        )
        if (uiEditor.isNew) setupForNewItem() else setupForExistItem()
    }
}