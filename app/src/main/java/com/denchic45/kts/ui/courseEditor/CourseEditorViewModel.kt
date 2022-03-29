package com.denchic45.kts.ui.courseEditor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.repository.SameCoursesException
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.login.groupChooser.GroupChooserInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class CourseEditorViewModel @Inject constructor(
    @Named(CourseEditorFragment.COURSE_ID)
    courseId: String?,
    private val interactor: CourseEditorInteractor,
    private val confirmInteractor: ConfirmInteractor,
    private var groupChooserInteractor: GroupChooserInteractor
) : BaseViewModel() {
    val selectSubject = MutableLiveData<Subject>()
    val selectTeacher = MutableLiveData<User>()
    val nameField = MutableLiveData<String>()
    val showFoundTeachers = SingleLiveData<List<ListItem>>()
    val showFoundSubjects = SingleLiveData<List<ListItem>>()

    val subjectNameTypeEnable = MutableLiveData<Boolean>()
    val teacherNameTypeEnable = MutableLiveData<Boolean>()
    val groupList = MutableLiveData(addAdderGroupItem())
    val title = MutableLiveData<String>()
    val openChoiceOfGroup = SingleLiveData<Unit>()

    private val courseId: String = courseId ?: UUID.randomUUID().toString()
    private var foundTeachers: List<User>? = null
    private var foundSubjects: List<Subject>? = null
    private val subjectId: String? = null
    private val teacherId: String? = null

    private val typedSubjectName = MutableSharedFlow<String>()
    private val typedTeacherName = MutableSharedFlow<String>()
    private var groupHeaders: MutableList<GroupHeader> = mutableListOf()
    private val uiEditor: UIEditor<Course> = UIEditor(courseId == null) {
        Course(
            this.courseId,
            nameField.value ?: "",
            selectSubject.value ?: Subject.createEmpty(),
            selectTeacher.value ?: User.createEmpty(),
            groupHeaders
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
                    .map { list ->
                        foundTeachers = list
                        list.map { user: User ->
                            ListItem(
                                id = user.id,
                                title = user.fullName,
                                icon = EitherMessage.String(user.photoUrl),
                                type = ListPopupWindowAdapter.TYPE_AVATAR
                            )
                        }
                    }
                    .collect { t: List<ListItem> -> showFoundTeachers.setValue(t) }
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showToast(R.string.error_check_network)
                }
            }
        }

        viewModelScope.launch {
            try {
                typedSubjectName
                    .flatMapLatest { name: String -> interactor.findSubjectByTypedName(name) }
                    .map { list ->
                        foundSubjects = list
                        list.map { (id, name, iconUrl) ->
                            ListItem(
                                id = id,
                                title = name,
                                icon = EitherMessage.String(iconUrl)
                            )
                        }
                    }
                    .collect { t: List<ListItem> -> showFoundSubjects.setValue(t) }
            } catch (e: Exception) {
                if (e is NetworkException) {
                    showToast(R.string.error_check_network)
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
                interactor.findCourse(courseId).collect { course: Course? ->
                    course?.let {
                        uiEditor.oldItem = course
                        nameField.value = course.name
                        selectTeacher.value = course.teacher
                        selectSubject.value = course.subject
                        groupHeaders = course.groupHeaders.toMutableList()
                        groupList.value = addAdderGroupItem(groupHeaders)
                    } ?: finish()
                }
            }
        }

    private fun addAdderGroupItem(groupHeaders: List<GroupHeader> = emptyList()): List<DomainModel> =
        groupHeaders.map { ListItem(id = it.id, title = it.name, type = 1) } + ListItem(
            id = "ADD_GROUP",
            title = "Добавить",
            icon = EitherMessage.Id(R.drawable.ic_add)
        )

    private fun onSaveClick() {
        viewModelScope.launch {
            try {
                if (uiEditor.isNew)
                    interactor.addCourse(uiEditor.item)
                else
                    interactor.updateCourse(uiEditor.item)
                finish()
            } catch (e: Exception) {
                when (e) {
                    is NetworkException -> {
                        showToast(R.string.error_check_network)
                    }
                    is SameCoursesException -> {
                        showSnackBar("Такой курс уже существует!")
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
        viewModelScope.launch {
            delay(500) // Needed for waiting menu of this fragment
            setSaveOptionVisibility(uiValidator.runValidates() && uiEditor.hasBeenChanged())
        }
    }

    private fun setSaveOptionVisibility(visible: Boolean) {
        viewModelScope.launch {
            optionVisibility.emit(R.id.option_course_save to visible)
        }
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
        viewModelScope.launch {
            groupChooserInteractor.receiveSelectedGroup()
                .let { courseGroup ->
                    groupHeaders.add(courseGroup)
                    groupList.value = addAdderGroupItem(groupHeaders)
                    enablePositiveBtn()
                }
        }
    }

    fun onGroupRemoveClick(position: Int) {
        groupHeaders.removeAt(position)
        groupList.value = addAdderGroupItem(groupHeaders)
        enablePositiveBtn()
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
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
        viewModelScope.launch {
            if (uiEditor.hasBeenChanged() || !uiEditor.isNew) {
                openConfirmation(
                    "Удаление курса" to "Удаленный курс нельзя будет восстановить"
                )
                if (confirmInteractor.receiveConfirm()) {
                    removeCourse()
                }
            } else {
                removeCourse()
            }
        }
    }

    private fun confirmFinish() {
        viewModelScope.launch {
            when {
                uiEditor.isNew -> {
                    openConfirmation(Pair("Закрыть редактор курса", "Новый курс не будет сохранен"))
                    if (confirmInteractor.receiveConfirm()) {
                        finish()
                    }
                }
                uiEditor.hasBeenChanged() -> {
                    openConfirmation(
                        "Закрыть редактор курса" to
                                "Изменения курса не будут сохранены"
                    )
                    if (confirmInteractor.receiveConfirm()) {
                        finish()
                    }
                }
                else -> finish()
            }
        }
    }

    private suspend fun removeCourse() {
        try {
            finish()
            interactor.removeCourse(uiEditor.item)
        } catch (e: Exception) {
            if (e is NetworkException) {
                showToast(R.string.error_check_network)
            }
        }
    }

    override fun onCreateOptions() {
        super.onCreateOptions()
        viewModelScope.launch { optionVisibility.emit(R.id.option_course_delete to !uiEditor.isNew) }
    }
}