package com.denchic45.kts.ui.course.sections

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.domain.usecase.AddCourseSectionsUseCase
import com.denchic45.kts.domain.usecase.FindCourseSectionsUseCase
import com.denchic45.kts.domain.usecase.RemoveCourseSectionsUseCase
import com.denchic45.kts.domain.usecase.UpdateCourseSectionsUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Orders
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class CourseSectionEditorViewModel @Inject constructor(
    @Named(CourseSectionEditorFragment.COURSE_ID)
    private val courseId: String,
    findCourseSectionsUseCase: FindCourseSectionsUseCase,
    private val addCourseSectionsUseCase: AddCourseSectionsUseCase,
    val updateCourseSectionsUseCase: UpdateCourseSectionsUseCase,
    val removeCourseSectionsUseCase: RemoveCourseSectionsUseCase
) : BaseViewModel() {

    private var oldPosition = -1
    private var position = -1

    val sections = MutableSharedFlow<List<Section>>(replay = 1)

    init {
        viewModelScope.launch {
            sections.emitAll(findCourseSectionsUseCase(courseId))
        }
    }

    fun onSectionMove(oldPosition: Int, position: Int) {
        if (this.oldPosition == -1)
            this.oldPosition = oldPosition
        this.position = position

        viewModelScope.launch {
            Collections.swap(sections.first(), oldPosition, position)
            sections.emit(sections.first())
        }
    }

    fun onSectionMoved() {
        if (oldPosition == position)
            return

        viewModelScope.launch {
            val sections = sections.first()

            val prevOrder = if (position == 0) 0 else sections[position - 1].order

            val nextOrder =
                if (position == sections.size - 1) sections[position - 1].order + (1024 * 2)
                else sections[position + 1].order




            updateCourseSectionsUseCase(
                sections.toMutableList().apply {
                    set(
                        position,
                        sections[position].copy(
                            order = Orders.getBetweenOrders(
                                prevOrder,
                                nextOrder
                            )
                        )
                    )
                }
            )
        }
        oldPosition = -1
        position = -1
    }

    fun onSectionAdd(name: String) {
        if (name.isEmpty()) return
        viewModelScope.launch {
            addCourseSectionsUseCase(
                Section(courseId, name, sections.first().last().order + 1024)
            )
        }
    }

    fun onSectionRename(name: String, position: Int) {
        viewModelScope.launch {
            updateCourseSectionsUseCase(
                sections.first().toMutableList().apply {
                    set(
                        position,
                        this[position].copy(name = name)
                    )
                }
            )
        }
    }

    fun onSectionRemove(position: Int) {
        viewModelScope.launch { removeCourseSectionsUseCase(sections.first()[position]) }
    }

}