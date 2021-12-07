package com.denchic45.kts.ui.login.choiceOfGroup

import android.app.Application
import androidx.lifecycle.*
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.utils.PredicateUtil
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class ChoiceOfGroupViewModel @Inject constructor(
    application: Application,
    private val interactor: ChoiceOfGroupInteractor
) : AndroidViewModel(application) {
    val finish = SingleLiveData<Void>()
    private val expandableSpecialties: MutableMap<String, Boolean> = HashMap()
    private var allSpecialties: LiveData<List<Specialty>>
    private var groupsBySpecialty: LiveData<List<Group>>
    private var selectedSpecialtyUuid = MutableLiveData<String>()
    var groupAndSpecialtyList = MediatorLiveData<MutableList<DomainModel>>()


    @Contract("_ -> param1")
    private fun sortedList(list: MutableList<DomainModel>): MutableList<DomainModel> {
        list.sortWith(Comparator.comparing { o: DomainModel -> getSpecialtyUuid(o) })
        return list
    }

    private fun getSpecialtyUuid(o: Any): String {
        if (o is Specialty) {
            return o.uuid
        } else if (o is Group) {
            return o.specialty.uuid
        }
        throw IllegalStateException()
    }

    fun onSpecialtyItemClick(position: Int) {
        val (specialityUuid, specialtyName) = groupAndSpecialtyList.value!![position] as Specialty
        if (expandableSpecialties[specialtyName]!!) {
            groupAndSpecialtyList.value!!.removeAll(getGroupListBySpecialtyUuid(specialityUuid))
            groupAndSpecialtyList.setValue(groupAndSpecialtyList.value)
        } else {
            selectedSpecialtyUuid.setValue(specialityUuid)
        }
        expandableSpecialties.replace(specialtyName, !expandableSpecialties[specialtyName]!!)
    }

    fun onGroupItemClick(position: Int) {
        val groupUuid = groupAndSpecialtyList.value!![position].uuid
        interactor.findGroupInfoByUuid(groupUuid)
        interactor.postSelectGroupEvent((groupAndSpecialtyList.value!![position] as Group))
        finish.call()
    }

    private fun getGroupListBySpecialtyUuid(specialtyUuid: String): List<Group> {
        val groupListBySpecialty: MutableList<Group> = ArrayList()
        for (o in groupAndSpecialtyList.value!!) {
            if (o is Group) {
                if (o.specialty.uuid == specialtyUuid) {
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
        groupsBySpecialty = Transformations.switchMap(selectedSpecialtyUuid) { uuid: String? ->
            interactor.findGroupsBySpecialtyUuid(uuid)
        }
        allSpecialties = interactor.allSpecialties
        groupAndSpecialtyList.addSource(allSpecialties) { specialtyList: List<Specialty> ->
            for ((_, name) in specialtyList) {
                expandableSpecialties[name] = false
            }
            groupAndSpecialtyList.setValue(sortedList(ArrayList(specialtyList)))
        }
        groupAndSpecialtyList.addSource(groupsBySpecialty) { groups: List<Group>? ->
            groupAndSpecialtyList.value!!
                .addAll(groups!!)
            groupAndSpecialtyList.setValue(
                sortedList(
                    groupAndSpecialtyList.value!!.stream()
                        .filter(PredicateUtil.distinctByKey(DomainModel::uuid))
                        .collect(Collectors.toList())
                )
            )
        }
    }
}