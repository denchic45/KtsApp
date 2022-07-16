package com.denchic45.kts.ui.userEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.domain.model.User.Companion.isStudent
import com.denchic45.kts.domain.model.User.Companion.isTeacher
import com.denchic45.kts.domain.usecase.FindGroupByContainsNameUseCase
import com.denchic45.kts.domain.usecase.FindGroupNameUseCase
import com.denchic45.kts.domain.usecase.RemoveStudentUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.login.groupChooser.GroupChooserInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.UUIDS
import com.denchic45.kts.util.Validations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

open class UserEditorViewModel @Inject constructor(
    @Named("UserEditor ${UserEditorFragment.USER_ID}") userId: String?,
    @Named(UserEditorFragment.USER_ROLE) role: String?,
    @Named(UserEditorFragment.USER_GROUP_ID) private var groupId: String?,
    @Named("genders") genderList: List<ListItem>,
    private val interactor: UserEditorInteractor,
    private val removeStudentUseCase: RemoveStudentUseCase,
    private val confirmInteractor: ConfirmInteractor,
    private val findGroupByContainsNameUseCase: FindGroupByContainsNameUseCase,
    private val findGroupNameUseCase: FindGroupNameUseCase,
    private val groupChooserInteractor: GroupChooserInteractor
) : BaseViewModel() {

    val roles = MutableStateFlow(0)

    val genders = MutableLiveData<List<ListItem>>()

    val groupList = MutableLiveData<List<ListItem>>()

    val firstNameField = MutableStateFlow("")

    val surnameField = MutableStateFlow("")

    val patronymicField = MutableStateFlow("")

    val emailField = MutableStateFlow("")

    val passwordField = MutableStateFlow("")

    val genderField = MutableStateFlow("")

    val roleField = MutableStateFlow(R.string.role_student)

    val groupField = MutableStateFlow("")

    val avatarUser = MutableStateFlow("")

//    val emailFieldEnable = MutableStateFlow(true)

//    val passwordFieldVisibility = MutableStateFlow(true)

    val roleFieldVisibility = MutableStateFlow(false)

    val groupFieldVisibility = MutableStateFlow(false)

    val accountFieldsVisibility = MutableStateFlow(true)

    val errorMessageField = SingleLiveData<Pair<Int, String?>>()

    private val typedNameGroup = MutableSharedFlow<String>()

    private val genderList: List<ListItem>
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<User>
    private var userId: String
    private var role: User.Role
    private var gender = 0
    private var admin = false
    private var generatedAvatar = true
    private var password: String = ""
    private fun getUiValidator(): UIValidator {
        val uiValidator: UIValidator = UIValidator.of(
            Validation(Rule({
                firstNameField.value.isNotEmpty()
            }, "Имя обязательно"))
                .sendMessageResult(R.id.til_firstName, errorMessageField),
            Validation(Rule({
                !TextUtils.isEmpty(
                    surnameField.value
                )
            }, "Фамилия обязательна"))
                .sendMessageResult(R.id.til_surname, errorMessageField),
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
                .sendMessageResult(R.id.til_gender, errorMessageField),
//            Validation(
//                Rule({ !TextUtils.isEmpty(role) }, "Роль обязательна")
//            ).sendMessageResult(R.id.til_role, fieldErrorMessage),
            Validation(Rule({
                isTeacher(
                    role
                ) || isStudent(role) && !groupId.isNullOrEmpty()
            }, "Группа отсутствует"))
                .sendMessageResult(R.id.til_group, errorMessageField),
            Validation(
                Rule({
                    !accountFieldsVisibility.value || Validations.validEmail(
                        emailField.value
                    ) && accountFieldsVisibility.value
                }, "Некоректная почта!")
            )
                .sendMessageResult(R.id.til_email, errorMessageField),
            Validation(
                Rule(
                    { !accountFieldsVisibility.value || !TextUtils.isEmpty(password) && accountFieldsVisibility.value },
                    "Некоректный пароль!"
                ),
                Rule(
                    { !accountFieldsVisibility.value || accountFieldsVisibility.value && password.length > 5 },
                    "Минимальный размер пароля - 6 символов!"
                )
            )
                .sendMessageResult(R.id.til_password, errorMessageField)
        )
        return uiValidator
    }

    init {
        this.role = role?.let { User.Role.valueOf(it) } ?: User.Role.TEACHER
        this.userId = userId ?: UUIDS.createShort()
        this.genderList = genderList
        uiEditor = UIEditor(userId == null) {
            User(
                this.userId,
                firstNameField.value,
                surnameField.value,
                patronymicField.value,
                groupId,
                this.role,
                emailField.value,
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
        if (isStudent(role)) {
            setGroupView()
        }
        setRoleView()
        setAvailableRoles()
        groupFieldVisibility.value = isStudent(role)
        roleFieldVisibility.value = isTeacher(role)
        genders.value = genderList
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
        toolbarTitle = when (role) {
            User.Role.STUDENT -> "Новый студент"
            User.Role.TEACHER -> "Новый преподаватель"
            else -> throw IllegalStateException()
        }
    }

    private fun setupForExistItem() {
        getExistUser()
//        emailFieldEnable.value = false
        accountFieldsVisibility.value = false
        toolbarTitle = when (role) {
            User.Role.STUDENT -> {
                "Редактировать студента"
            }
            User.Role.TEACHER, User.Role.HEAD_TEACHER -> {
                "Редактировать руководителя"
            }
        }
    }

    private fun setGroupView() {
        groupId?.let {
            viewModelScope.launch {
                findGroupNameUseCase(it).collect {
                    groupField.value = it
                }
            }
        }
    }

    private fun setRoleView() {
        roleField.value = when (role) {
            User.Role.STUDENT -> R.string.role_student
            User.Role.TEACHER -> R.string.role_teacher
            User.Role.HEAD_TEACHER -> R.string.role_headTeacher
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
                    firstNameField.value = user.firstName
                    surnameField.value = user.surname
                    patronymicField.value = user.patronymic ?: ""
                    genderField.value = genders.value!![gender - 1].title
                    emailField.value = user.email!!
                    avatarUser.value = user.photoUrl
                } ?: run { finish() }
            }
        }
    }

    fun onRoleSelect(roleItem: ListItem) {
        role = User.Role.valueOf(roleItem.content as String)

    }

    fun onGenderSelect(position: Int) {
        val item = genderList[position]
        gender = (item.content as Double).toInt()

        genderField.value = item.title
    }

    fun onGroupSelect(position: Int) {
        val item = groupList.value!![position]
        groupId = item.id
        groupField.value = item.title
    }

    fun onFirstNameType(firstName: String) {
        firstNameField.value = firstName
    }

    fun onSurnameType(surname: String) {
        surnameField.value = surname
    }

    fun onPatronymicType(patronymic: String) {
        patronymicField.value = patronymic
    }

//    fun onPhoneNumType(phoneNum: String) {
//        fieldPhoneNum.value = phoneNum
//    }

    fun onEmailType(email: String) {
        emailField.value = email
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
                interactor.signUpUser(emailField.value, password)
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
            viewModelScope.launch { finish() }
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
                    if (isStudent(role))
                        removeStudentUseCase(uiEditor.item.id)
                    else if (isTeacher(role))
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
        if (isStudent(role)) {
            roles.value = R.raw.roles_student
        } else if (isTeacher(role)) {
            roles.value = R.raw.roles_teacher
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

    fun onPasswordType(password: String) {
        this.password = password
    }

    fun onSelectGroupClick() {
        viewModelScope.launch {
            navigateTo(MobileNavigationDirections.actionGlobalGroupChooserFragment())
            groupChooserInteractor.receiveSelectedGroup().let {
                groupId = it.id
                groupField.value = it.name
            }
        }
    }

    fun onPasswordGenerateClick() {
        passwordField.value = randomAlphaNumericString(16)
    }

    private fun randomAlphaNumericString(desiredStrLength: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..desiredStrLength)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}