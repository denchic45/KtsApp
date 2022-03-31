package com.denchic45.kts.ui.userEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.domain.User.Companion.isTeacher
import com.denchic45.kts.domain.usecase.FindGroupByContainsNameUseCase
import com.denchic45.kts.domain.usecase.FindGroupNameUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.UUIDS
import com.denchic45.kts.utils.Validations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

open class UserEditorViewModel @Inject constructor(
    @Named("UserEditor ${UserEditorFragment.USER_ID}") userId: String?,
    @Named(UserEditorFragment.USER_ROLE) role: String,
    @Named(UserEditorFragment.USER_GROUP_ID) private var groupId: String?,
    @Named("genders") genderList: List<ListItem>,
    private val interactor: UserEditorInteractor,
    private val confirmInteractor: ConfirmInteractor,
    private val findGroupByContainsNameUseCase: FindGroupByContainsNameUseCase,
    private val findGroupNameUseCase: FindGroupNameUseCase
) : BaseViewModel() {

    val fieldRoles = MutableStateFlow(0)

    val fieldGenders = MutableLiveData<List<ListItem>>()

    val groupList = MutableLiveData<List<ListItem>>()

    val fieldFirstName = MutableStateFlow("")

    val fieldSurname = MutableStateFlow("")

    val fieldPatronymic = MutableStateFlow("")

//    val fieldPhoneNum = MutableStateFlow("")

    val fieldEmail = MutableStateFlow("")

    val fieldGender = MutableStateFlow("")

    val fieldRole = MutableStateFlow(R.string.role_student)

    val fieldGroup = MutableStateFlow("")

    val avatarUser = MutableStateFlow("")

    val fieldGroupVisibility = MutableStateFlow(false)

    val fieldEmailEnable = MutableStateFlow(true)

    val fieldPasswordVisibility = MutableStateFlow(true)

    val fieldErrorMessage = SingleLiveData<Pair<Int, String?>>()

    private val typedNameGroup = MutableSharedFlow<String>()

    private val genderList: List<ListItem>
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<User>

    //    var groupName: LiveData<String>? = null
    private var userId: String
    private var role: String?
    private var gender = 0
    private var admin = false
    private var generatedAvatar = true
    private var password: String? = null
    private fun getUiValidator(): UIValidator {
        val uiValidator: UIValidator = UIValidator.of(
            Validation(Rule({
                fieldFirstName.value.isNotEmpty()
            }, "Имя обязательно"))
                .sendMessageResult(R.id.til_firstName, fieldErrorMessage),
            Validation(Rule({
                !TextUtils.isEmpty(
                    fieldSurname.value
                )
            }, "Фамилия обязательна"))
                .sendMessageResult(R.id.til_surname, fieldErrorMessage),
//            Validation(
//                Rule({
//                    !TextUtils.isEmpty(
//                        fieldPhoneNum.value
//                    )
//                }, "Номер обязателен"),
//                Rule({
//                    Validations.validPhoneNumber(
//                        fieldPhoneNum.value
//                    )
//                }, "Номер некоректный")
//            ).sendMessageResult(R.id.til_phoneNum, fieldErrorMessage),
            Validation(Rule({ gender != 0 }, "Пол обязателен"))
                .sendMessageResult(R.id.til_gender, fieldErrorMessage),
            Validation(
                Rule({ !TextUtils.isEmpty(role) }, "Роль обязательна")
            ).sendMessageResult(R.id.til_role, fieldErrorMessage),
            Validation(Rule({
                isTeacher(
                    role!!
                ) || isStudent(role!!) && !groupId.isNullOrEmpty()
            }, "Группа отсутствует"))
                .sendMessageResult(R.id.til_group, fieldErrorMessage),
            Validation(
                Rule({
                    !fieldEmailEnable.value || Validations.validEmail(
                        fieldEmail.value
                    ) && fieldEmailEnable.value
                }, "Некоректная почта!")
            )
                .sendMessageResult(R.id.til_email, fieldErrorMessage),
            Validation(
                Rule(
                    { !fieldPasswordVisibility.value || !TextUtils.isEmpty(password) && fieldPasswordVisibility.value },
                    "Некоректный пароль!"
                ),
                Rule(
                    { !fieldPasswordVisibility.value || fieldPasswordVisibility.value && password!!.length > 5 },
                    "Минимальный размер пароля - 6 символов!"
                )
            )
                .sendMessageResult(R.id.til_password, fieldErrorMessage)
        )
        return uiValidator
    }

    init {
        this.role = role
        this.userId = userId ?: UUIDS.createShort()
        this.genderList = genderList
        uiEditor = UIEditor(userId == null) {
            User(
                this.userId,
                fieldFirstName.value,
                fieldSurname.value,
                fieldPatronymic.value,
                groupId,
                role,
//                PhoneNumberUtils.normalizeNumber(fieldPhoneNum.value),
                fieldEmail.value,
                "",
                Date(),
                gender,
                generatedAvatar,
                admin
            )
        }
        uiValidator = getUiValidator()
        setup()
    }

    private fun setup() {
        if (isStudent(role!!)) {
            setGroupView()
        }
        setRoleView()
        setAvailableRoles()
        fieldGroupVisibility.value = isStudent(role!!)
        fieldGenders.value = genderList
        viewModelScope.launch {
            typedNameGroup.flatMapLatest { name: String -> findGroupByContainsNameUseCase(name) }
                .collect { resource ->
                    if (resource is Resource.Success) {
                        groupList.value = resource.data.map { group ->
                            ListItem(
                                id = group.id,
                                title = group.name
                            )
                        }
                    }
                }
        }
        if (uiEditor.isNew) {
            setupForNewItem()
        } else {
            setupForExistItem()
        }
    }

    private fun setupForNewItem() {
        toolbarTitle = when {
            isStudent(role!!) -> "Новый студент"
            isTeacher(role!!) -> "Новый преподаватель"
            else -> throw IllegalStateException()
        }
    }

    private fun setupForExistItem() {
        getExistUser()
        fieldEmailEnable.value = false
        fieldPasswordVisibility.value = false
        toolbarTitle = when {
            isStudent(role!!) -> "Редактировать студента"
            isTeacher(role!!) -> "Редактировать преподавателя"
            else -> throw IllegalStateException()
        }
    }

    private fun setGroupView() {
        groupId?.let {
            viewModelScope.launch {
                findGroupNameUseCase(it).collect {
                    fieldGroup.value = it
                }
            }
        }
    }

    private fun setRoleView() {
        fieldRole.value = when (role) {
            User.STUDENT -> R.string.role_student
            User.DEPUTY_MONITOR -> R.string.role_deputyMonitor
            User.CLASS_MONITOR -> R.string.role_classMonitor
            User.TEACHER -> R.string.role_teacher
            User.HEAD_TEACHER -> R.string.role_headTeacher
            else -> throw IllegalStateException("Unknow role: $role")
        }
    }

    private fun getExistUser() {
        viewModelScope.launch {
            interactor.observeUserById(userId).collect { user ->
                user?.let {
                    uiEditor.oldItem = user
                    role = user.role
                    gender = user.gender
                    admin = user.admin
                    generatedAvatar = user.generatedAvatar
                    fieldFirstName.value = user.firstName
                    fieldSurname.value = user.surname
                    fieldPatronymic.value = user.patronymic ?: ""
                    fieldGender.value = fieldGenders.value!![gender - 1].title
//                    fieldPhoneNum.value = user.phoneNum
                    fieldEmail.value = user.email!!
                    avatarUser.value = user.photoUrl
                } ?: run { finish() }
            }
        }
    }

    fun onRoleSelect(roleItem: ListItem) {
        role = roleItem.content as String

    }

    fun onGenderSelect(position: Int) {
        val item = genderList[position]
        gender = (item.content as Double).toInt()

        fieldGender.value = item.title
    }

    fun onGroupSelect(position: Int) {
        val item = groupList.value!![position]
        groupId = item.id
        fieldGroup.value = item.title
    }

    fun onFirstNameType(firstName: String) {
        fieldFirstName.value = firstName
    }

    fun onSurnameType(surname: String) {
        fieldSurname.value = surname
    }

    fun onPatronymicType(patronymic: String) {
        fieldPatronymic.value = patronymic
    }

//    fun onPhoneNumType(phoneNum: String) {
//        fieldPhoneNum.value = phoneNum
//    }

    fun onEmailType(email: String) {
        fieldEmail.value = email
    }

    fun onGroupNameType(groupName: String) {
        viewModelScope.launch { typedNameGroup.emit(groupName) }
    }

    private fun onFabClick() {
        uiValidator.runValidates {
            if (uiEditor.hasBeenChanged())
                saveChanges()
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            if (uiEditor.isNew) {
                interactor.signUpUser(fieldEmail.value, password!!)
                interactor.addUser(uiEditor.item)
            } else {
                interactor.updateUser(uiEditor.item)
            }.collect { resource: Resource<User> ->
                when (resource) {
                    is Resource.Success -> finish()
                    is Resource.Next -> {
                        if (resource.status == "LOAD_AVATAR") avatarUser.value =
                            resource.data.photoUrl
                    }
                    is Resource.Error -> if (resource.error is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_user_save -> onFabClick()
            R.id.option_user_delete -> confirmDelete()
        }
    }

    fun onBackPress() {
        if (uiEditor.hasBeenChanged()) {
            if (uiEditor.isNew) confirmExit(
                Pair(
                    "Отменить создание?",
                    "Новый пользователь не будет сохранен"
                )
            ) else confirmExit(
                Pair("Отменить редактирование?", "Внесенные изменения не будут сохранены")
            )
        } else {
            finish()
        }
    }

    private fun confirmDelete() {
        openConfirmation(
            Pair(
                "Удаление пользователя",
                "Удаленного пользователя нельзя будет восстановить"
            )
        )

        viewModelScope.launch {
            if (confirmInteractor.receiveConfirm()) {
                try {
                    if (isStudent(role!!))
                        interactor.removeStudent(uiEditor.item)
                    else if (isTeacher(role!!))
                        interactor.removeTeacher(uiEditor.item)
                    finish()
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun confirmExit(titleWithSubtitlePair: Pair<String, String>) {
        viewModelScope.launch {
            openConfirmation(titleWithSubtitlePair)
            if (confirmInteractor.receiveConfirm()) {
                finish()
            }
        }
    }

    private fun setAvailableRoles() {
        if (role != null) {
            if (isStudent(role!!)) {
                fieldRoles.value = R.raw.roles_student
            } else if (isTeacher(role!!)) {
                fieldRoles.value = R.raw.roles_teacher
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        if (uiEditor.isNew) {
            viewModelScope.launch {
                setMenuItemVisible(R.id.option_user_delete to false)
            }
        }
    }

    fun onPasswordType(password: String?) {
        this.password = password
    }
}