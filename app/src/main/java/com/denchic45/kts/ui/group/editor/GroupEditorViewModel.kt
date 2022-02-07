package com.denchic45.kts.ui.group.editor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.LiveDataUtil
import com.denchic45.kts.utils.NetworkException
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Named

class GroupEditorViewModel @Inject constructor(
    @Named("GroupEditor ${GroupPreference.GROUP_ID}") id: String?,
    private val choiceOfCuratorInteractor: ChoiceOfCuratorInteractor,
    private val interactor: GroupEditorInteractor,
    @Named("courses") val courseList: List<ListItem>
) : BaseViewModel() {
    val enableSpecialtyField = MutableLiveData<Boolean>()
    val toolbarTitle = MutableLiveData<String>()
    val showMessageId = SingleLiveData<Int>()
    val nameField = MutableLiveData<String>()
    val specialtyField = MutableLiveData<String>()
    val showSpecialties = MutableLiveData<List<ListItem>>()
    val courseField = MutableLiveData<String>()
    val curatorField = MutableLiveData<User>()
    val fieldErrorMessage = SingleLiveData<Pair<Int, String>>()
    val deleteOptionVisibility = MutableLiveData<Boolean>()
    val openChoiceOfCurator = SingleLiveData<Void>()
    private val typedSpecialtyByName = MutableSharedFlow<String>()
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Group>
    private val id: String = id ?: UUID.randomUUID().toString()
    private var course: Int = 0

    private var subscribeConfirmation: Disposable? = null
    private var foundSpecialties: List<Specialty>? = null
    private var specialty: Specialty? = null

    init {
        viewModelScope.launch {
            typedSpecialtyByName.flatMapLatest { specialtyName: String ->
                interactor.getSpecialtyByTypedName(specialtyName)
            }.collect { resource ->
                foundSpecialties = (resource as Resource.Success).data
                showSpecialties.postValue(
                    resource.data.stream()
                        .map { specialty: Specialty ->
                            ListItem(id = specialty.id, title = specialty.name)
                        }
                        .collect(Collectors.toList())
                )
            }
        }
        uiEditor = UIEditor(id == null) {
            Group(
                this.id,
                nameField.value ?: "",
                course,
                specialty ?: Specialty.createEmpty(),
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
                    { !TextUtils.isEmpty(specialtyField.value) },
                    "Специальность обязательна"
                )
            )
                .sendMessageResult(R.id.til_specialty, fieldErrorMessage),
            Validation(Rule({ !TextUtils.isEmpty(courseField.value) }, "Курс группы обязателен"))
                .sendMessageResult(R.id.til_course, fieldErrorMessage),
            Validation(Rule({ curatorField.value != null }, R.string.error_not_curator))
                .sendMessageIdResult(showMessageId)
        )
        if (uiEditor.isNew) setupForNewItem() else setupForExistItem()
    }

    private fun setupForNewItem() {
        toolbarTitle.value = "Создать группу"
    }

    private fun setupForExistItem() {
        existGroup
        enableSpecialtyField.value = false
        toolbarTitle.value = "Редактировать группу"
    }

    private val existGroup: Unit
        get() {
            LiveDataUtil.observeOnce(interactor.findGroup(id)) { group: Group ->
                uiEditor.oldItem = group
                curatorField.value = group.curator
                nameField.value = group.name
                specialtyField.value = group.specialty.name
                courseField.setValue(courseList[group.course - 1].title)
            }
        }

    fun onCourseSelect(position: Int) {
        courseField.value = courseList[position].title
        course = (courseList[position].content as Double).toInt()
        //        uiEditor.getItem().setCourse(((Double) courseList.get(position).getContent()).intValue());
    }

    fun onGroupNameType(name: String) {
        nameField.postValue(name)
        //        uiEditor.getItem().setName(name);
    }

    fun onSpecialtySelect(position: Int) {
        specialty = foundSpecialties!![position]
        //        uiEditor.getItem().setSpecialty(specialty);
        specialtyField.value = specialty!!.name
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

    fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_delete_group -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        openConfirmation.value =
            Pair("Удаление пользователя", "Удаленного пользователя нельзя будет восстановить")
        subscribeConfirmation = RxBusConfirm.getInstance()
            .event
            .subscribe { confirm: Boolean ->
                viewModelScope.launch {
                    if (confirm) {
                        try {
                            interactor.removeGroup(uiEditor.item)
                            finish.call()
                        } catch (e: Exception) {
                            if (e is NetworkException) {
                                showMessageId.value = R.string.error_check_network
                            }
                        }
                    }
                }
                subscribeConfirmation!!.dispose()
            }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        if (uiEditor.isNew) {
            deleteOptionVisibility.value = false
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

    fun onCuratorClick() {
        openChoiceOfCurator.call()
        choiceOfCuratorInteractor.observeSelectedCurator()
            .take(1)
            .subscribe { teacher: User ->
                uiEditor.item.curator = teacher
                curatorField.setValue(teacher)
            }
    }

    fun onFabClick() {
        uiValidator.runValidates { saveChanges() }
    }

    private fun saveChanges() {
        val observer: DisposableCompletableObserver = object : DisposableCompletableObserver() {
            override fun onComplete() {
                finish.call()
            }

            override fun onError(throwable: Throwable) {
                if (throwable is NetworkException) {
                    showMessageId.setValue(R.string.error_check_network)
                } else {
                    throw RuntimeException(throwable)
                }
            }
        }
        if (uiEditor.isNew) {
            interactor.addGroup(uiEditor.item)
                .subscribe(observer)
        } else {
            interactor.updateGroup(uiEditor.item)
                .subscribe(observer)
        }
    }
}