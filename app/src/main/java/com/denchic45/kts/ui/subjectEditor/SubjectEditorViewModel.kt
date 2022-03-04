package com.denchic45.kts.ui.subjectEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.EitherResource
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.domain.onId
import com.denchic45.kts.data.repository.SameSubjectIconException
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.iconPicker.IconPickerInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.LiveDataUtil
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.UUIDS
import com.denchic45.kts.utils.colors
import io.reactivex.rxjava3.disposables.Disposable
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

    val currentSelectedColor = MutableLiveData(0)

    val openIconPicker = SingleLiveData<Void>()


    val showColors = MutableLiveData<Pair<List<ListItem>, Int>>()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Subject>
    private val id: String = subjectId ?: UUIDS.createShort()
    private var colorName = ""

    private var subscribe: Disposable? = null
    fun onColorSelect(position: Int) {
        val item: ListItem = colors[position]
        colorName = item.title
        item.color.onId { colorIcon.setValue(it) }
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
                    showMessage.setValue("Проверьте подключение к интернету")
                }
                is SameSubjectIconException -> {
                    showMessage.value = "Такая иконка уже используется!"
                }
            }

        }
    }

    private fun setupForNewItem() {
        title.value = "Создать предмет"
        colorName = "blue"
        deleteBtnVisibility.value = false
        showColors.value = Pair(colors, 0)
    }

    private fun setupForExistItem() {
        title.value = "Редактировать предмет"
        LiveDataUtil.observeOnce(interactor.find(id)) { subject: Subject ->
            uiEditor.oldItem = subject
            nameField.value = subject.name
            colorIcon.value = findColorId(subject.colorName)
            currentSelectedColor.value = IntStream.range(0, colors.size)
                .filter { value: Int -> colors[value].title == subject.colorName }
                .findFirst()
                .orElse(-1)
            icon.value = subject.iconUrl
            colors.stream()
                .filter { voidListItem -> voidListItem.title == subject.colorName }
                .findFirst()
                .ifPresent { colorItem ->
                    colorItem.color.onId {
                        colorIcon.value = it
                        showColors.setValue(Pair(colors, colors.indexOf(colorItem)))
                    }
                }
        }
    }

    private fun findColorId(colorName: String): Int {
        return colors.stream()
            .filter { listItem -> listItem.title == colorName }
            .map { (it.color as EitherResource.Id).a }
            .findFirst()
            .orElse(R.color.blue)
    }

    fun onNameType(name: String) {
        nameField.value = name
        enablePositiveBtn.postValue(uiValidator.runValidates())
    }

    fun onPositiveClick() {
        uiValidator.runValidates {
            viewModelScope.launch { saveChanges() }
        }
    }

    fun onDeleteClick() {
        openConfirmation(Pair("Удалить несколько предметов группы", "Вы точно уверены???"))
        viewModelScope.launch {
            if (confirmInteractor.awaitConfirm()) {
                try {
                    interactor.remove(uiEditor.oldItem!!)
                    finish()
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showMessage.value = "Проверьте подключение к интернету"
                    }
                }
            }
        }
    }

    fun onIconClick() {
        openIconPicker.call()
        subscribe = iconPickerInteractor.observeSelectedIcon()!!.subscribe { iconUrl: String ->
            subscribe!!.dispose()
            icon.value = iconUrl
            enablePositiveBtn.postValue(uiValidator.runValidates())
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
            Validation(Rule({ !nameField.value.isNullOrEmpty() }, "Нет названия")),
            Validation(Rule({ !icon.value.isNullOrEmpty() }, "Нет иконки"))
        )
        if (uiEditor.isNew) setupForNewItem() else setupForExistItem()
    }
}