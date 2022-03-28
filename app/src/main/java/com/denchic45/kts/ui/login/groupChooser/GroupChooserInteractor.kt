package com.denchic45.kts.ui.login.groupChooser

import com.denchic45.kts.data.model.domain.GroupHeader
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.repository.GroupRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChooserInteractor @Inject constructor(
    private val groupRepository: GroupRepository
) {
    private val selectedGroup = Channel<GroupHeader>()

    suspend fun findGroupsBySpecialtyId(id: String): List<GroupHeader> {
        return groupRepository.findBySpecialtyId(id)
    }

    val allSpecialties: Flow<List<Specialty>> = groupRepository.findAllSpecialties()

    fun findGroupInfoById(groupId: String) {
        groupRepository.findGroupInfoById(groupId)
    }

    suspend fun receiveSelectedGroup(): GroupHeader {
        return selectedGroup.receive()
    }

    suspend fun postSelectGroup(groupHeader: GroupHeader) {
        selectedGroup.send(groupHeader)
    }
}