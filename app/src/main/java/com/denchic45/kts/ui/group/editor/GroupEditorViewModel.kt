package com.denchic45.kts.ui.group.editor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.UUIDS
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupEditorViewModel @Inject constructor(
    @Named(GroupEditorFragment.GROUP_ID) id: String?,
    private val choiceOfCuratorInteractor: ChoiceOfCuratorInteractor,
    private val addGroupUseCase: AddGroupUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val removeGroupUseCase: RemoveGroupUseCase,
    private val findGroupUseCase: FindGroupUseCase,
    private val confirmInteractor: ConfirmInteractor,
    private val findSpecialtyByTypedNameUseCase: FindSpecialtyByTypedNameUseCase,
    @Named("courses") val courseList: List<ListItem>
) : BaseViewModel() {
    val enableSpecialtyField = MutableLiveData<Boolean>()
    val showMessageId = SingleLiveData<Int>()
    val nameField = MutableLiveData<String>()
    val specialtyField = MutableLiveData<Specialty>()
    val showSpecialties = MutableLiveData<List<ListItem>>()
    val courseField = MutableLiveData<String>()
    val curatorField = MutableLiveData<User>()
    val fieldErrorMessage = SingleLiveData<Pair<Int, String>>()
    val deleteOptionVisibility = MutableLiveData<Boolean>()
    val openChoiceOfCurator = SingleLiveData<Void>()
    private val typedSpecialtyByName = MutableSharedFlow<String>()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Group>
    private val id: String = id ?: UUIDS.createShort()
    private var course: Int = 0

    private var foundSpecialties: List<Specialty>? = null

    companion object {
        const val GROUP_ID = "GroupEditor GROUP_ID"
    }

    init {
        viewModelScope.launch {
            typedSpecialtyByName.flatMapLatest { specialtyName: String ->
                findSpecialtyByTypedNameUseCase(specialtyName)
            }.collect { list ->
                foundSpecialties = list
                showSpecialties.postValue(
                    list.map { specialty ->
                        ListItem(id = specialty.id, title = specialty.name)
                    }
                )
            }
        }
        uiEditor = UIEditor(id == null) {
            Group(
                this.id,
                nameField.value ?: "",
                course,
                specialtyField.value ?: Specialty.createEmpty(),
                curatorField.value ?: User.createEmpty()

            )
        }
        uiValidator = UIValidator.of(
            Validation(Rule { uiEditor.hasBeenChanged() }),
            Validation(
                Rule(
                    { !TextUtils.isEmpty(uiEditor.item.name) },
                    "Название группы обязательно"
                )
            )
                .sendMessageResult(R.id.til_group_name, fieldErrorMessage),
            Validation(
                Rule(
                    { specialtyField.value != null },
                    "Специальность обязательна"
                )
            )
                .sendMessageResult(R.id.til_specialty, fieldErrorMessage),
            Validation(Rule({ !TextUtils.isEmpty(courseField.value) }, "Курс группы обязателен"))
                .sendMessageResult(R.id.til_course, fieldErrorMessage),
            Validation(Rule({ curatorField.value != null }, R.string.error_not_curator))
                .sendMessageIdResult(showMessageId)
        )

        if (uiEditor.isNew)
            setupForNewItem()
        else
            setupForExistItem()

    }

    private fun setupForNewItem() {
        toolbarTitle = "Создать группу"
    }

    private fun setupForExistItem() {
        existGroup()
        enableSpecialtyField.value = false
        toolbarTitle = "Редактировать группу"
    }

    private fun existGroup() {
        viewModelScope.launch {
            findGroupUseCase(id).collect { group ->
                uiEditor.oldItem = group
                uiEditor.oldItem = group
                curatorField.value = group.curator
                nameField.value = group.name
                specialtyField.value = group.specialty
                courseField.value = courseList[group.course - 1].title

                course = group.course
            }
        }
    }

    fun onCourseSelect(position: Int) {
        courseField.value = courseList[position].title
        course = (courseList[position].content as Double).toInt()
    }

    fun onGroupNameType(name: String) {
        nameField.postValue(name)
    }

    fun onSpecialtySelect(position: Int) {
        specialtyField.value = foundSpecialties!![position]
    }

    fun onSpecialtyNameType(specialtyName: String) {
        viewModelScope.launch { typedSpecialtyByName.emit(specialtyName) }
    }

    fun onBackPress() {
        if (uiEditor.isNew) confirmExit(
            Pair(
                "Отменить создание?",
                "Новый пользователь не будет сохранен"
            )
        ) else confirmExit(
            Pair("Отменить редактирование?", "Внесенные изменения не будут сохранены")
        )
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_delete_group -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            openConfirmation(
                "Удаление пользователя" to
                        "Удаленного пользователя нельзя будет восстановить"
            )
            if (confirmInteractor.awaitConfirm()) {
                try {
                    removeGroupUseCase(uiEditor.item)
                    finish()
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showMessageId.value = R.string.error_check_network
                    }
                }
            }
        }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        if (uiEditor.isNew) {
            deleteOptionVisibility.value = false
        }
    }

    private fun confirmExit(titleWithSubtitlePair: Pair<String, String>) {
        viewModelScope.launch {
            openConfirmation(titleWithSubtitlePair)
            if (confirmInteractor.awaitConfirm())
                finish()
        }
    }

    fun onCuratorClick() {
        viewModelScope.launch {
            openChoiceOfCurator.call()
            choiceOfCuratorInteractor.awaitSelectTeacher().apply {
                uiEditor.item.curator = this
                curatorField.setValue(this)
            }
        }
    }

    fun onFabClick() {
        uiValidator.runValidates { saveChanges() }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            try {
                if (uiEditor.isNew) {
                    addGroupUseCase(uiEditor.item)
                } else {
                    updateGroupUseCase(uiEditor.item)
                }
                finish()
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showMessageId.value = R.string.error_check_network
                }
            }
        }
    }
}