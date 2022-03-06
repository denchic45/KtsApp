package com.denchic45.kts.ui.login.choiceOfGroup

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.GroupRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class ChoiceOfGroupInteractor constructor(
    private val groupRepository: GroupRepository
) : Interactor {

    private var selectedGroup: PublishSubject<CourseGroup>? = PublishSubject.create()
    fun findGroupsBySpecialtyId(id: String): LiveData<List<CourseGroup>> {
        return groupRepository.findBySpecialtyId(id)
    }

    val allSpecialties: LiveData<List<Specialty>>
        get() = groupRepository.allSpecialties

    fun findGroupInfoById(groupId: String) {
        groupRepository.findGroupInfoById(groupId)
    }

    override fun removeListeners() {
        selectedGroup!!.onComplete()
        selectedGroup = null
        groupRepository.removeListeners()
    }

    fun observeSelectedGroup(): Observable<CourseGroup> {
        if (selectedGroup == null) {
            selectedGroup = PublishSubject.create()
        }
        return selectedGroup!!
    }

    fun postSelectGroupEvent(group: CourseGroup) {
        selectedGroup!!.onNext(group)
    }
}