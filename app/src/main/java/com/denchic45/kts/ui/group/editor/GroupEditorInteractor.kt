package com.denchic45.kts.ui.group.editor

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.SpecialtyRepository
import com.denchic45.kts.data.repository.UserRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.ObservableSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupEditorInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val specialtyRepository: SpecialtyRepository,
    private val courseRepository: CourseRepository
) : Interactor() {

    fun findGroup(groupName: String?): LiveData<Group> {
        return groupInfoRepository.find(groupName!!)
    }

    fun getCuratorOfGroup(groupUuid: String): LiveData<User> {
        return groupInfoRepository.findCurator(groupUuid)
    }

    override fun removeListeners() {}
    fun getSpecialtyByTypedName(specialtyName: String): Flow<Resource<List<Specialty>>> {
        return specialtyRepository.findByTypedName(specialtyName)
    }

    fun findUser(uuid: String?): LiveData<User> {
        return userRepository.getByUuid(uuid!!)
    }

    fun addGroup(group: Group?): Completable {
        return groupInfoRepository.add(group!!)
    }

    fun updateGroup(group: Group?): Completable {
        return groupInfoRepository.update(group!!)
    }

    suspend fun removeGroup(group: Group) {
        courseRepository.removeGroup(group)
        groupInfoRepository.remove(group)
    }
}