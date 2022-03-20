package com.denchic45.kts.ui.login.groupChooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set

class GroupChooserViewModel @Inject constructor(
    private val groupChooserInteractor: GroupChooserInteractor
) : BaseViewModel() {
    private val expandableSpecialties: MutableMap<String, Boolean> = HashMap()

    private var selectedSpecialtyId = MutableSharedFlow<String>()

    private var groupsBySpecialty: Flow<List<CourseGroup>> =
        selectedSpecialtyId.flatMapLatest { id ->
            flow { emit(groupChooserInteractor.findGroupsBySpecialtyId(id)) }
        }

    private var _groupAndSpecialtyList = MutableStateFlow<List<DomainModel>>(emptyList())

    val groupAndSpecialtyList = _groupAndSpecialtyList.asStateFlow()

    init {
        viewModelScope.launch {
            _groupAndSpecialtyList.emitAll(groupAndSpecialtyList)
        }

        viewModelScope.launch {
            groupChooserInteractor.allSpecialties.collect { list ->
                for (specialty in list) {
                    expandableSpecialties[specialty.name] = false
                }
                _groupAndSpecialtyList.emit(sortedList(list))
            }
        }

        viewModelScope.launch {
            groupsBySpecialty.collect {
                val list = _groupAndSpecialtyList.value.plus(it)
                _groupAndSpecialtyList.emit(
                    sortedList(list.distinctBy(DomainModel::id))
                )
            }
        }
    }


    private fun sortedList(list: List<DomainModel>): List<DomainModel> {
        return list.sortedWith(compareBy { item ->
            when (item) {
                is Specialty -> item.id
                is CourseGroup -> item.specialtyId
                else -> throw IllegalStateException("Not correct type: $item")
            }
        })
    }

    fun onSpecialtyItemClick(position: Int) {
        viewModelScope.launch {
            val (specialityId, specialtyName) = _groupAndSpecialtyList.value[position] as Specialty
            if (expandableSpecialties.getValue(specialtyName)) {
                _groupAndSpecialtyList.emit(
                    _groupAndSpecialtyList.value.filterNot { it is CourseGroup && it.specialtyId == specialityId }
                )
            } else {
                selectedSpecialtyId.emit(specialityId)
            }
            expandableSpecialties.replace(specialtyName, !expandableSpecialties[specialtyName]!!)
        }
    }

    fun onGroupItemClick(position: Int) {
        val groupId = _groupAndSpecialtyList.value[position].id
        groupChooserInteractor.findGroupInfoById(groupId)
        viewModelScope.launch {
            finish()
            groupChooserInteractor.postSelectGroup((_groupAndSpecialtyList.value[position] as CourseGroup))
        }
    }
}