package com.denchic45.kts.ui.login.choiceOfGroup

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.GroupRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class ChoiceOfGroupInteractor constructor (
    private val groupRepository: GroupRepository,
    private val groupInfoRepository: GroupInfoRepository
) : Interactor() {

    private var selectedGroup = PublishSubject.create<Group>()
    fun findGroupsBySpecialtyUuid(uuid: String?): LiveData<List<Group>> {
        return groupRepository.findBySpecialtyUuid(uuid)
    }

    val allSpecialties: LiveData<List<Specialty>>
        get() = groupRepository.allSpecialties

    fun findGroupInfoByUuid(groupUuid: String?) {
        groupInfoRepository.findGroupInfoByUuid(groupUuid)
    }

    override fun removeListeners() {
        selectedGroup.onComplete()
        selectedGroup = null
        groupInfoRepository.removeListeners()
        groupRepository.removeListeners()
    }

    fun observeSelectedGroup(): Observable<Group> {
        if (selectedGroup == null) {
            selectedGroup = PublishSubject.create()
        }
        return selectedGroup
    }

    fun postSelectGroupEvent(group: Group) {
        selectedGroup.onNext(group)
    }
}