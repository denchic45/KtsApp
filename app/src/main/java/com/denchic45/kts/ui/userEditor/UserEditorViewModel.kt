package com.denchic45.kts.ui.userEditor

import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.domain.User.Companion.isTeacher
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.LiveDataUtil
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.UUIDS
import com.denchic45.kts.utils.Validations
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Named

open class UserEditorViewModel @Inject constructor(
    @Named("UserEditor ${UserEditorActivity.USER_UUID}") userUuid: String?,
    @Named(UserEditorActivity.USER_ROLE) role: String,
    @Named(UserEditorActivity.USER_GROUP_UUID) private var groupUuid: String?,
    @Named("genders") genderList: List<ListItem>,
    private val interactor: UserEditorInteractor
) : BaseViewModel() {

    val title = MutableLiveData<String>()

    val fieldRoles = MutableLiveData<Int>()

    val fieldGenders = MutableLiveData<List<ListItem>>()

    val groupList = MutableLiveData<List<ListItem>>()

    val fieldFirstName = MutableLiveData<String>()

    val fieldSurname = MutableLiveData<String>()

    val fieldPatronymic = MutableLiveData<String>()

    val fieldPhoneNum = MutableLiveData<String>()

    val fieldEmail = MutableLiveData<String>()

    val fieldGender = MutableLiveData<String>()

    val fieldRole = MutableLiveData<Int>()

    val fieldGroup = MutableLiveData<String>()

    val avatarUser = MutableLiveData<String>()

    val deleteOptionVisibility = SingleLiveData<Boolean>()

    val fieldGroupVisibility = MutableLiveData<Boolean>()

    val fieldEmailEnable = MutableLiveData(true)

    val fieldPasswordVisibility = MutableLiveData(true)

    val fieldErrorMessage = SingleLiveData<Pair<Int, String>>()

    private val typedNameGroup = MutableSharedFlow<String>()

    private val genderList: List<ListItem>
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<User>
    var groupName: LiveData<String>? = null
    private var userUuid: String
    private var role: String?
    private var gender = 0
    private var admin = false
    private var generatedAvatar = true
    private var subscribeConfirmation: Disposable? = null
    private var password: String? = null
    private fun getUiValidator(): UIValidator {
        val uiValidator: UIValidator = UIValidator.of<Any>(
            Validation(Rule({
                !TextUtils.isEmpty(
                    fieldFirstName.value
                )
            }, "Имя обязательно"))
                .sendMessageResult(R.id.til_firstName, fieldErrorMessage),
            Validation(Rule({
                !TextUtils.isEmpty(
                    fieldSurname.value
                )
            }, "Фамилия обязательна"))
                .sendMessageResult(R.id.til_surname, fieldErrorMessage),
            Validation(
                Rule({
                    !TextUtils.isEmpty(
                        fieldPhoneNum.value
                    )
                }, "Номер обязателен"),
                Rule({
                    Validations.isValidPhoneNumber(
                        fieldPhoneNum.value
                    )
                }, "Номер некоректный")
            )
                .sendMessageResult(R.id.til_phoneNum, fieldErrorMessage),
            Validation(Rule({ gender != 0 }, "Пол обязателен"))
                .sendMessageResult(R.id.til_gender, fieldErrorMessage),
            Validation(Rule({ !TextUtils.isEmpty(role) }, "Роль обязательна"))
                .sendMessageResult(R.id.til_role, fieldErrorMessage),
            Validation(Rule({
                isTeacher(
                    role!!
                ) || isStudent(role!!) && !groupUuid.isNullOrEmpty()
            }, "Группа отсутствует"))
                .sendMessageResult(R.id.til_group, fieldErrorMessage),
            Validation(
                Rule({
                    !fieldEmailEnable.value!! || Validations.isValidEmail(
                        fieldEmail.value
                    ) && fieldEmailEnable.value!!
                }, "Некоректная почта!")
            )
                .sendMessageResult(R.id.til_email, fieldErrorMessage),
            Validation(
                Rule(
                    { !fieldPasswordVisibility.value!! || !TextUtils.isEmpty(password) && fieldPasswordVisibility.value!! },
                    "Некоректный пароль!"
                )
            )
                .sendMessageResult(R.id.til_password, fieldErrorMessage)
        )
        return uiValidator
    }

    init {
        this.role = role
        this.userUuid = userUuid ?: UUIDS.createShort()
        this.genderList = genderList
        uiEditor = UIEditor(userUuid == null) {
            User(
                this.userUuid,
                fieldFirstName.value ?: "",
                fieldSurname.value ?: "",
                fieldPatronymic.value,
                groupUuid,
                role,
                PhoneNumberUtils.normalizeNumber(fieldPhoneNum.value ?: ""),
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
            typedNameGroup.flatMapLatest { name: String -> interactor.getGroupsByTypedName(name) }
                .map { resource: Resource<List<Group>> ->
                    resource.data.stream()
                        .map { group: Group -> ListItem(uuid = group.uuid, title = group.name) }
                        .collect(Collectors.toList())
                }
                .collect { value: List<ListItem> -> groupList.setValue(value) }
        }
        if (uiEditor.isNew) {
            setupForNewItem()
        } else {
            setupForExistItem()
        }
    }

    private fun setupForNewItem() {
        if (isStudent(role!!)) {
            title.value = "Новый студент"
        } else if (isTeacher(role!!)) {
            title.value = "Новый преподаватель"
        }
    }

    private fun setupForExistItem() {
        existUser
        fieldEmailEnable.value = false
        fieldPasswordVisibility.value = false
        if (isStudent(role!!)) {
            title.setValue("Редактировать студента")
        } else if (isTeacher(role!!)) {
            title.value = "Редактировать преподавателя"
        }
    }

    private fun setGroupView() {
        if (groupUuid != null) {
            groupName = interactor.getGroupNameByUuid(groupUuid!!)
            LiveDataUtil.observeOnce(interactor.getGroupNameByUuid(groupUuid!!)) { value: String ->
                fieldGroup.value = value
            }
        }
    }

    private fun setRoleView() {
        when (role) {
            User.STUDENT -> fieldRole.setValue(R.string.role_student)
            User.DEPUTY_MONITOR -> fieldRole.setValue(R.string.role_deputyMonitor)
            User.CLASS_MONITOR -> fieldRole.setValue(R.string.role_classMonitor)
            User.TEACHER -> fieldRole.setValue(R.string.role_teacher)
            User.HEAD_TEACHER -> fieldRole.setValue(R.string.role_headTeacher)
        }
    }

    private val existUser: Unit
        get() {
            LiveDataUtil.observeOnce(interactor.getUserByUuid(userUuid)) { user: User ->
                uiEditor.oldItem = user
                role = user.role
                gender = user.gender
                admin = user.admin
                generatedAvatar = user.generatedAvatar
                fieldFirstName.value = user.firstName
                fieldSurname.value = user.surname
                fieldPatronymic.value = user.patronymic ?: ""
                fieldGender.value = fieldGenders.value!![gender - 1].title
                fieldPhoneNum.value = user.phoneNum
                fieldEmail.value = user.email!!
                avatarUser.setValue(user.photoUrl)
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
        groupUuid = item.uuid
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

    fun onPhoneNumType(phoneNum: String) {
        fieldPhoneNum.value = phoneNum
    }

    fun onEmailType(email: String) {
        fieldEmail.value = email
    }

    fun onGroupNameType(groupName: String) {
        viewModelScope.launch { typedNameGroup.emit(groupName) }
    }

    fun onFabClick() {
        uiValidator.runValidates { if (uiEditor.hasBeenChanged()) saveChanges() }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val saveUserObservable: Flow<Resource2<User>> = if (uiEditor.isNew) {
                interactor.signUpUser(fieldEmail.value, password)
                interactor.addUser(uiEditor.item)
            } else {
                interactor.updateUser(uiEditor.item)
            }
            saveUserObservable.collect { resource: Resource2<User> ->
                when (resource) {
                    is Resource2.Success -> finish.call()
                    is Resource2.Next -> {
                        if (resource.status == "LOAD_AVATAR") avatarUser.value = resource.data.photoUrl
                    }
                    is Resource2.Error -> if (resource.error is NetworkException) {
                        showMessage.value = "Отсутствует интернет-соединение"
                    }
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_delete_user -> confirmDelete()
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
            finish.call()
        }
    }

    private fun confirmDelete() {
        openConfirmation.value =
            Pair("Удаление пользователя", "Удаленного пользователя нельзя будет восстановить")
        subscribeConfirmation = RxBusConfirm.getInstance()
            .event
            .subscribe { confirm: Boolean ->
                if (confirm) {
                    if (isStudent(role!!)) interactor.removeStudent(uiEditor.item)
                        .subscribe() else if (isTeacher(
                            role!!
                        )
                    ) interactor.removeTeacher(uiEditor.item).subscribe()
                    finish.call()
                }
                subscribeConfirmation!!.dispose()
            }
    }

    private fun confirmExit(titleWithSubtitlePair: Pair<String, String>) {
        openConfirmation.value = titleWithSubtitlePair
        subscribeConfirmation = RxBusConfirm.getInstance()
            .event
            .subscribe { confirm: Boolean ->
                if (confirm) {
                    finish.call()
                }
                subscribeConfirmation!!.dispose()
            }
    }

    private fun setAvailableRoles() {
        if (role != null) {
            if (isStudent(role!!)) {
                fieldRoles.setValue(R.raw.roles_student)
            } else if (isTeacher(role!!)) {
                fieldRoles.value = R.raw.roles_teacher
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onCreateOptions() {
        if (uiEditor.isNew) {
            deleteOptionVisibility.value = false
        }
    }

    fun onPasswordType(password: String?) {
        this.password = password
    }
}