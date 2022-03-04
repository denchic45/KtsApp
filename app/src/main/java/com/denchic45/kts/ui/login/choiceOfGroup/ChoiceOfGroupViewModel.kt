package com.denchic45.kts.ui.login.choiceOfGroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.PredicateUtil
import org.jetbrains.annotations.Contract
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.collections.set

class ChoiceOfGroupViewModel @Inject constructor(
    private val interactor: ChoiceOfGroupInteractor
) : BaseViewModel() {
    private val expandableSpecialties: MutableMap<String, Boolean> = HashMap()
    private var allSpecialties: LiveData<List<Specialty>>
    private var groupsBySpecialty: LiveData<List<CourseGroup>>
    private var selectedSpecialtyId = MutableLiveData<String>()
    var groupAndSpecialtyList = MediatorLiveData<MutableList<DomainModel>>()


    private fun sortedList(list: MutableList<DomainModel>): MutableList<DomainModel> {
        list.sortWith(Comparator.comparing { o: DomainModel -> getSpecialtyId(o) })
        return list
    }

    private fun getSpecialtyId(o: DomainModel): String {
        if (o is Specialty) {
            return o.id
        } else if (o is CourseGroup) {
            return o.specialtyId
        }
        throw IllegalStateException("Not correct type: $o")
    }

    fun onSpecialtyItemClick(position: Int) {
        val (specialityId, specialtyName) = groupAndSpecialtyList.value!![position] as Specialty
        if (expandableSpecialties[specialtyName]!!) {
            groupAndSpecialtyList.value!!.removeAll(getGroupListBySpecialtyId(specialityId))
            groupAndSpecialtyList.setValue(groupAndSpecialtyList.value)
        } else {
            selectedSpecialtyId.setValue(specialityId)
        }
        expandableSpecialties.replace(specialtyName, !expandableSpecialties[specialtyName]!!)
    }

    fun onGroupItemClick(position: Int) {
        val groupId = groupAndSpecialtyList.value!![position].id
        interactor.findGroupInfoById(groupId)
        interactor.postSelectGroupEvent((groupAndSpecialtyList.value!![position] as CourseGroup))
         finish()
    }

    private fun getGroupListBySpecialtyId(specialtyId: String): List<Group> {
        val groupListBySpecialty: MutableList<Group> = ArrayList()
        for (o in groupAndSpecialtyList.value!!) {
            if (o is Group) {
                if (o.specialty.id == specialtyId) {
                    groupListBySpecialty.add(o)
                }
            }
        }
        return groupListBySpecialty
    }

    public override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    init {
        groupsBySpecialty = Transformations.switchMap(selectedSpecialtyId) { id: String ->
            interactor.findGroupsBySpecialtyId(id)
        }
        allSpecialties = interactor.allSpecialties
        groupAndSpecialtyList.addSource(allSpecialties) { specialtyList: List<Specialty> ->
            for ((_, name) in specialtyList) {
                expandableSpecialties[name] = false
            }
            groupAndSpecialtyList.setValue(sortedList(ArrayList(specialtyList)))
        }
        groupAndSpecialtyList.addSource(groupsBySpecialty) { groups ->
            groupAndSpecialtyList.value!!
                .addAll(groups)
            groupAndSpecialtyList.setValue(
                sortedList(
                    groupAndSpecialtyList.value!!.stream()
                        .filter(PredicateUtil.distinctByKey(DomainModel::id))
                        .collect(Collectors.toList())
                )
            )
        }
    }
}