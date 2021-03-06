package com.denchic45.kts.ui.group.editor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.domain.model.Group
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.domain.model.Specialty
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.domain.usecase.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.teacherChooser.TeacherChooserInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.UUIDS
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GroupEditorViewModel @Inject constructor(
    @Named(GroupEditorFragment.GROUP_ID) id: String?,
    private val teacherChooserInteractor: TeacherChooserInteractor,
    private val addGroupUseCase: AddGroupUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val removeGroupUseCase: RemoveGroupUseCase,
    private val findGroupUseCase: FindGroupUseCase,
    private val confirmInteractor: ConfirmInteractor,
    private val findSpecialtyByContainsNameUseCase: FindSpecialtyByContainsNameUseCase,
    @Named("courses") val courseList: List<ListItem>
) : BaseViewModel() {
    val enableSpecialtyField = MutableLiveData<Boolean>()
    val nameField = MutableLiveData<String>()
    val specialtyField = MutableLiveData<Specialty>()
    val showSpecialties = MutableLiveData<List<ListItem>>()
    val courseField = MutableLiveData<String>()
    val curatorField = MutableLiveData<User>()
    val fieldErrorMessage = SingleLiveData<Pair<Int, String?>>()
    val openTeacherChooser = SingleLiveData<Void>()
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
                findSpecialtyByContainsNameUseCase(specialtyName)
            }.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        foundSpecialties = resource.data
                        showSpecialties.postValue(
                            resource.data.map { specialty ->
                                ListItem(id = specialty.id, title = specialty.name)
                            }
                        )
                    }
                }
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
                    "???????????????? ???????????? ??????????????????????"
                )
            )
                .sendMessageResult(R.id.til_group_name, fieldErrorMessage),
            Validation(
                Rule(
                    { specialtyField.value != null },
                    "?????????????????????????? ??????????????????????"
                )
            )
                .sendMessageResult(R.id.til_specialty, fieldErrorMessage),
            Validation(Rule({ !TextUtils.isEmpty(courseField.value) }, "???????? ???????????? ????????????????????"))
                .sendMessageResult(R.id.til_course, fieldErrorMessage),
            Validation(Rule({ curatorField.value != null }, R.string.error_not_curator))
                .onErrorRun { showSnackBar(R.string.error_not_curator) }
        )

        if (uiEditor.isNew)
            setupForNewItem()
        else
            setupForExistItem()

    }

    private fun setupForNewItem() {
        toolbarTitle = "?????????????? ????????????"
    }

    private fun setupForExistItem() {
        existGroup()
        enableSpecialtyField.value = false
        toolbarTitle = "?????????????????????????? ????????????"
    }

    private fun existGroup() {
        viewModelScope.launch {
            findGroupUseCase(id).collect { group ->
                group?.let {
                    uiEditor.oldItem = group
                    uiEditor.oldItem = group
                    curatorField.value = group.curator
                    nameField.value = group.name
                    specialtyField.value = group.specialty
                    courseField.value = courseList[group.course - 1].title

                    course = group.course
                } ?: finish()
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
                "???????????????? ?????????????????",
                "?????????? ???????????????????????? ???? ?????????? ????????????????"
            )
        ) else confirmExit(
            Pair("???????????????? ?????????????????????????????", "?????????????????? ?????????????????? ???? ?????????? ??????????????????")
        )
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_group_delete -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            openConfirmation(
                "???????????????? ????????????????????????" to
                        "???????????????????? ???????????????????????? ???????????? ?????????? ????????????????????????"
            )
            if (confirmInteractor.receiveConfirm()) {
                try {
                    finish()
                    removeGroupUseCase(uiEditor.item.id)
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                }
            }
        }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        setMenuItemVisible(R.id.option_group_delete to !uiEditor.isNew)
    }

    private fun confirmExit(titleWithSubtitlePair: Pair<String, String>) {
        viewModelScope.launch {
            openConfirmation(titleWithSubtitlePair)
            if (confirmInteractor.receiveConfirm())
                finish()
        }
    }

    fun onCuratorClick() {
        viewModelScope.launch {
            openTeacherChooser.call()
            teacherChooserInteractor.receiveSelectTeacher().apply {
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
                    showToast(R.string.error_check_network)
                }
            }
        }
    }
}