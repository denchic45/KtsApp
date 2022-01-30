package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.lessonEditor

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.Lesson
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject.ChoiceOfSubjectInteractor
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class LessonEditorViewModel @Inject constructor(
    private val interactor: EventEditorInteractor
) : BaseViewModel() {

    val subjectField = MutableLiveData<Subject>()

    val openChoiceOfTeacher = SingleLiveData<Void>()

    val teachersField = MutableLiveData<MutableList<User>>()

    val showErrorField = MutableLiveData<Pair<Int, Boolean>>()

    val openChoiceOfGroupSubject = SingleLiveData<Void>()
    private val uiEditor: UIEditor<Lesson> = UIEditor(interactor.isNew) {
        Lesson(
            subjectField.value ?: Subject.createEmpty(),
            teachersField.value ?: emptyList()
        )
    }

    @Inject
    lateinit var choiceOfSubjectInteractor: ChoiceOfSubjectInteractor

    @Inject
    lateinit var choiceOfCuratorInteractor: ChoiceOfCuratorInteractor

    private var selectedSubjectSubscribe: Disposable? = null
    private fun fillFields() {
        if (interactor.oldEvent.value!!.details is Lesson && interactor.oldEvent.value!!.details != Lesson.createEmpty()) {
            (interactor.oldEvent.value!!.details as Lesson).apply {
                subjectField.value = subject
                teachersField.value = teachers.toMutableList()
            }
        }
    }

    fun onSubjectClick() {
        choiceOfSubjectInteractor.groupName = interactor.oldEvent.value!!.group.name
        choiceOfSubjectInteractor.groupId = interactor.oldEvent.value!!.group.id
        selectedSubjectSubscribe =
            choiceOfSubjectInteractor.observeSelectedSubject().subscribe { subject: Subject ->
                selectedSubjectSubscribe!!.dispose()
                subjectField.value = subject
            }
        openChoiceOfGroupSubject.call()
    }

    fun onAddTeacherItemClick() {
        openChoiceOfTeacher.call()
        choiceOfCuratorInteractor.observeSelectedCurator()
            .take(1)
            .subscribe { teacher: User ->
                teachersField.value =
                    teachersField.value?.apply { add(teacher) } ?: mutableListOf(teacher)
            }
    }

    fun onRemoveTeacherItemClick(position: Int) {
        teachersField.value = uiEditor.item.teachers.toMutableList().apply { removeAt(position) }
    }

    init {
        interactor.getDetails = { uiEditor.item }

        with(interactor.oldEvent.value!!.details) {
            if (this.type == EventEntity.TYPE.LESSON) {
                uiEditor.oldItem = interactor.oldEvent.value!!.details as Lesson
            }
        }


        val uiValidator = UIValidator.of(
            Validation(Rule { subjectField.value != null })
                .sendActionResult(
                    { showErrorField.setValue(Pair(R.id.rl_subject, true)) }
                ) { showErrorField.setValue(Pair(R.id.rl_subject, false)) },
            Validation(
                Rule({ !teachersField.value.isNullOrEmpty() }, "Нет преподавателя")
            ).sendMessageResult(showMessage)
        )
        interactor.validateEventDetails = { uiValidator.runValidates() }
        fillFields()
    }
}