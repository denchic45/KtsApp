package com.denchic45.kts.ui.course.sectionPicker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.domain.usecase.FindCourseSectionsUseCase
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class SectionPickerViewModel @Inject constructor(
    findCourseSectionsUseCase: FindCourseSectionsUseCase,
    @Named(TaskEditorFragment.COURSE_ID) courseId: String
) : ViewModel() {

    private val _selectedSection = Channel<Section>()
    val selectedSectionId = _selectedSection.receiveAsFlow()

    var currentSelectedSection = Section.createEmpty()
        set(value) {
            field = value
            postSectionPosition()
        }

    val selectedSectionPosition = MutableLiveData(0)

    val sections = MutableLiveData<List<String>>()
    private var _sections: List<Section> = emptyList()

    fun onSectionItemClick(position: Int) {
        currentSelectedSection =
            if (position == 0) Section.createEmpty()
            else _sections[position - 1]
        selectedSectionPosition.value = position
    }

    init {
        viewModelScope.launch {
            findCourseSectionsUseCase(courseId).collect { foundSections ->
                _sections = foundSections
                sections.value = listOf("Без секции") + foundSections.map { it.name }
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            _selectedSection.send(currentSelectedSection)
        }
    }

    private fun postSectionPosition() {
        selectedSectionPosition.value =
            if (currentSelectedSection == Section.createEmpty()) {
                0
            } else {
                _sections.indexOfFirst { it == currentSelectedSection } + 1
            }
    }
}