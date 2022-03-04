package com.denchic45.kts.ui.group.choiceOfCurator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChoiceOfCuratorViewModel @Inject constructor(
    private val interactor: ChoiceOfCuratorInteractor
) : BaseViewModel() {
    val showFoundTeachers = MutableLiveData<List<User>>()
    val showErrorNetworkState = SingleLiveData<Boolean>()
    private val queryTeachersByName = MutableSharedFlow<String>()


    fun onTeacherClick(position: Int) {
        val user = showFoundTeachers.value!![position]
        interactor.postSelectedCurator(user)
         finish()
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onTeacherNameType(name: String) {
        viewModelScope.launch { queryTeachersByName.emit(name) }
    }

    init {
        viewModelScope.launch {
            try {
                queryTeachersByName.filter { s: String -> s.length > 2 }
                    .flatMapLatest { name: String -> interactor.findTeacherByTypedName(name) }
                    .collect {
                        showErrorNetworkState.value = false
                        showFoundTeachers.setValue(it)
                    }
            } catch (e: Exception) {
                showFoundTeachers.value = emptyList()
                showErrorNetworkState.value = true
            }


        }
    }
}