package com.denchic45.kts.ui.courseEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.repository.SameCoursesException
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.NetworkException
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Named

class CourseEditorViewModel @Inject constructor(
    @Named(CourseEditorFragment.COURSE_ID)
    courseId: String?,
    private val interactor: CourseEditorInteractor,
    var choiceOfGroupInteractor: ChoiceOfGroupInteractor
) : BaseViewModel() {
    val selectSubject = MutableLiveData<Subject>()
    val selectTeacher = MutableLiveData<User>()
    val nameField = MutableLiveData<String>()
    val showFoundTeachers = SingleLiveData<List<ListItem>>()
    val showFoundSubjects = SingleLiveData<List<ListItem>>()

    val subjectNameTypeEnable = MutableLiveData<Boolean>()
    val teacherNameTypeEnable = MutableLiveData<Boolean>()
    val groupList = MutableLiveData(addAdderGroupItem())
    val optionVisibility = SingleLiveData<Pair<Int, Boolean>>()
    val title = MutableLiveData<String>()
    val openChoiceOfGroup = SingleLiveData<Unit>()

    private var subscribeConfirmation: Disposable? = null

    private val courseId: String = courseId ?: UUID.randomUUID().toString()
    var foundTeachers: List<User>? = null
    var foundSubjects: List<Subject>? = null
    private val subjectId: String? = null
    private val teacherId: String? = null

    private val typedSubjectName = MutableSharedFlow<String>()
    private val typedTeacherName = MutableSharedFlow<String>()
    private var groups: MutableList<CourseGroup> = mutableListOf()
    private val uiEditor: UIEditor<Course> = UIEditor(courseId == null) {
        Course(
            this.courseId,
            nameField.value ?: "",
            selectSubject.value ?: Subject.createEmpty(),
            selectTeacher.value ?: User.createEmpty(),
            groups
        )
    }

    init {
        setup()
    }

    private val uiValidator: UIValidator = UIValidator.of(
        Validation(Rule({ subjectId.isNullOrEmpty() }, "Предмет отсутствует")),
        Validation(Rule({ teacherId.isNullOrEmpty() }, "Преподаватель отсутствует"))
    )

    private fun setup() {

        viewModelScope.launch {
            try {
                typedTeacherName
                    .flatMapLatest { name -> interactor.findTeacherByTypedName(name) }
                    .map { resource: Resource<List<User>> ->
                        if (resource is Resource.Success) {
                            foundTeachers = resource.data
                            resource.data.stream()
                                .map { user: User ->
                                    ListItem(
                                        id = user.id,
                                        title = user.fullName,
                                        icon = EitherResource.String(user.photoUrl),
                                        type = ListPopupWindowAdapter.TYPE_AVATAR
                                    )
                                }
                                .collect(Collectors.toList())
                        } else throw IllegalStateException()
                    }
                    .collect { t: List<ListItem> -> showFoundTeachers.setValue(t) }
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showMessageRes.value = R.string.error_check_network
                }
            }
        }

        viewModelScope.launch {
            try {
                typedSubjectName
                    .flatMapLatest { name: String -> interactor.findSubjectByTypedName(name) }
                    .map { resource ->
                        foundSubjects = (resource as Resource.Success).data
                        resource.data.stream()
                            .map { (id, name, iconUrl) ->
                                ListItem(
                                    id = id,
                                    title = name,
                                    icon = EitherResource.String(iconUrl)
                                )
                            }
                            .collect(Collectors.toList())
                    }
                    .collect { t: List<ListItem> -> showFoundSubjects.setValue(t) }
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showMessageRes.value = R.string.error_check_network
                }
            }
        }

        if (uiEditor.isNew) setupForNewItem() else setupForExistItem()
    }

    private fun setupForNewItem() {
        title.value = "Добавить курс"
    }

    private fun setupForExistItem() {
        existCourse
        title.value = "Редактировать курс"
    }

    private val existCourse: Unit
        get() {
            viewModelScope.launch {
                interactor.findCourse(courseId).collect { course: Course ->
                    uiEditor.oldItem = course
                    nameField.value = course.name
                    selectTeacher.value = course.teacher
                    selectSubject.value = course.subject
                    groups = course.groups.toMutableList()
                    groupList.value = addAdderGroupItem(groups)
                }
            }
        }

    private fun addAdderGroupItem(groups: List<CourseGroup> = emptyList()): List<DomainModel> =
        groups.map { ListItem(id = it.id, title = it.name, type = 1) } + ListItem(
            id = "ADD_GROUP",
            title = "Добавить",
            icon = EitherResource.Id(R.drawable.ic_add)
        )

    private fun onSaveClick() {
        viewModelScope.launch {
            try {
                if (uiEditor.isNew) interactor.addCourse(uiEditor.item)
                else interactor.updateCourse(uiEditor.item)
                finish.call()
            } catch (e: Exception) {
                when (e) {
                    is NetworkException -> {
                        showMessageRes.value = R.string.error_check_network
                    }
                    is SameCoursesException -> {
                        showMessage.value = "Такой курс уже существует!"
                    }
                }
                e.printStackTrace()
            }
        }
    }

    fun onSubjectNameType(subjectName: String) {
        viewModelScope.launch { typedSubjectName.emit(subjectName) }
    }

    fun onTeacherNameType(teacherName: String) {
        viewModelScope.launch { typedTeacherName.emit(teacherName) }
    }

    fun onCourseNameType(name: String) {
        nameField.postValue(name)
        enablePositiveBtn()
    }

    fun onTeacherSelect(position: Int) {
        val teacher = foundTeachers!![position]
        teacherNameTypeEnable.value = false
        selectTeacher.value = teacher
        enablePositiveBtn()
    }

    private fun enablePositiveBtn() {
        viewModelScope.launch(Dispatchers.Main) {
            setSaveOptionVisibility(uiValidator.runValidates() && uiEditor.hasBeenChanged())
        }
    }

    private fun setSaveOptionVisibility(visible: Boolean) {
        optionVisibility.postValue(R.id.option_course_save to visible)
    }

    fun onSubjectSelect(position: Int) {
        val subject = foundSubjects!![position]
        subjectNameTypeEnable.value = false
        selectSubject.value = subject
        enablePositiveBtn()

        if ((nameField.value ?: "").isEmpty()) {
            nameField.value = subject.name
        }
    }

    fun onSubjectNameClick() {
        subjectNameTypeEnable.value = true
        optionVisibility
        setSaveOptionVisibility(false)
    }

    fun onTeacherNameClick() {
        teacherNameTypeEnable.value = true
        setSaveOptionVisibility(false)
    }

    fun onSubjectEditClick() {
        subjectNameTypeEnable.value = !(subjectNameTypeEnable.value ?: false)
        enablePositiveBtn()
    }

    fun onTeacherEditClick() {
        teacherNameTypeEnable.value = !(teacherNameTypeEnable.value ?: false)
        enablePositiveBtn()
    }

    fun onSubjectNameFocusChange(focus: Boolean) {
        subjectNameTypeEnable.value = focus
    }

    fun onTeacherNameFocusChange(focus: Boolean) {
        teacherNameTypeEnable.value = focus
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onGroupAddClick() {
        openChoiceOfGroup.call()
        choiceOfGroupInteractor.observeSelectedGroup()
            .subscribe {
                groups.add(it)
                groupList.value = addAdderGroupItem(groups)
                enablePositiveBtn()
            }
    }

    fun onGroupRemoveClick(position: Int) {
        groups.removeAt(position)
        groupList.value = addAdderGroupItem(groups)
        enablePositiveBtn()
    }

    fun onOptionClick(id: Int) {
        when (id) {
            android.R.id.home -> {
                confirmFinish()
            }
            R.id.option_course_save -> {
                onSaveClick()
            }
            R.id.option_course_delete -> {
                onDeleteClick()
            }
        }
    }

    private fun onDeleteClick() {
        if (uiEditor.hasBeenChanged() || !uiEditor.isNew) {
            openConfirmation.value =
                Pair(
                    "Удаление курса",
                    "Удаленный курс нельзя будет восстановить"
                )
            subscribeConfirmation = RxBusConfirm.getInstance()
                .event
                .subscribe { confirm: Boolean ->
                    if (confirm) {
                        deleteCourse()
                    }
                    subscribeConfirmation!!.dispose()
                }
        } else {
            deleteCourse()
        }
    }

    private fun confirmFinish() {
        when {
            uiEditor.isNew -> {
                openConfirmation.value =
                    Pair("Закрыть редактор курса", "Новый курс не будет сохранен")
                subscribeConfirmation = RxBusConfirm.getInstance()
                    .event.subscribe { confirm: Boolean ->
                        if (confirm) {
                            finish.call()
                        }
                        subscribeConfirmation!!.dispose()
                    }
            }
            uiEditor.hasBeenChanged() -> {
                openConfirmation.value =
                    Pair("Закрыть редактор курса", "Изменения курса не будут сохранены")
                subscribeConfirmation = RxBusConfirm.getInstance()
                    .event.subscribe { confirm: Boolean ->
                        if (confirm) {
                            finish.call()
                        }
                        subscribeConfirmation!!.dispose()
                    }
            }
            else -> finish.call()
        }
    }

    private fun deleteCourse() {
        viewModelScope.launch {
            try {
                interactor.removeCourse(uiEditor.item)
                finish.call()
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showMessageRes.value = R.string.error_check_network
                }
            }
        }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        optionVisibility.postValue(R.id.option_course_delete to !uiEditor.isNew)
    }
}