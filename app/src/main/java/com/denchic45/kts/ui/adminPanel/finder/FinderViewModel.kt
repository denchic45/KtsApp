package com.denchic45.kts.ui.adminPanel.finder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.Action
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.rx.bus.RxBusConfirm
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.utils.NetworkException
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Named

class FinderViewModel @Inject constructor(
    @Named("options_user") userOptions: List<ListItem>,
    @Named("options_group") groupOptions: List<ListItem>,
    @Named("options_subject") subjectOptions: List<ListItem>,
    private val interactor: FinderInteractor
) : BaseViewModel() {
    @JvmField
    val finderEntities = MutableLiveData<List<ListItem>>()

    @JvmField
    val currentSelectedEntity = MutableLiveData(POSITION_FIND_USERS)

    @JvmField
    val showFoundItems = MutableLiveData<List<DomainModel>>()

    @JvmField
    val showListEmptyState = MutableLiveData<Boolean>()

    @JvmField
    val openGroup = SingleLiveData<String>()

    @JvmField
    val openSubject = SingleLiveData<String>()

    @JvmField
    val openProfile = SingleLiveData<String>()

    @JvmField
    val openUserEditor = SingleLiveData<Map<String, String>>()

    @JvmField
    val openSubjectEditor = SingleLiveData<String>()

    @JvmField
    val openGroupEditor = SingleLiveData<String>()

    @JvmField
    val openSpecialtyEditor = SingleLiveData<String>()

    @JvmField
    val openCourse = SingleLiveData<String>()

    @JvmField
    val showOptions = SingleLiveData<Pair<Int, List<ListItem>>>()
    private val queryByName = MutableSharedFlow<String>()
    private val onFinderItemClickActions: List<Consumer<String>> = listOf(
        Consumer { t: String -> openProfile.setValue(t) },
        Consumer { t: String -> openGroup.setValue(t) },
        Consumer { t: String -> openSubject.setValue(t) },
        Consumer { t: String -> openSpecialtyEditor.setValue(t) },
        Consumer { t: String -> openCourse.setValue(t) })
    private val queryTexts = mutableListOf<String?>(null, null, null, null, null)
    private val startEmptyList: List<DomainModel> = emptyList()
    private val foundEntities = mutableListOf(
        startEmptyList,
        startEmptyList,
        startEmptyList,
        startEmptyList,
        startEmptyList
    )

    private val onOptionItemClickActions: Map<String, Action>

    private val findByTypedNameActions = listOf(
        { name: String -> interactor.findUserByTypedName(name) },
        { name: String -> interactor.findGroupByTypedName(name) },
        { name: String -> interactor.findSubjectByTypedName(name) },
        { name: String -> interactor.findSpecialtyByTypedName(name) },
        { name: String -> interactor.findCourseByTypedName(name) })
    private val optionsList: List<List<ListItem>>
    private var selectedEntity: DomainModel? = null
    private var subscribeConfirmation: Disposable? = null
    fun onQueryTextSubmit(queryName: String) {
        viewModelScope.launch {
            queryTexts[currentSelectedEntity.value!!] = queryName
            queryByName.emit(queryName)
        }
    }

    fun onFinderEntitySelect(position: Int) {
        currentSelectedEntity.value = position
        val items = foundEntities[currentSelectedEntity.value!!]
        showFoundItems.value = items
        if (items === startEmptyList || items.isNotEmpty()) {
            showListEmptyState.setValue(false)
        } else {
            showListEmptyState.setValue(true)
        }
        queryTexts[position]?.let {
            viewModelScope.launch {
                queryByName.emit(it)
            }
        }
        interactor.removeListeners()
    }

    fun onOptionClick(optionId: String) {
        onOptionItemClickActions[optionId]!!.run()
    }

    fun onFinderItemClick(position: Int) {
        val item = foundEntities[currentSelectedEntity.value!!][position]
        onFinderItemClickActions[currentSelectedEntity.value!!].accept(item.id)
    }

    fun onFinderItemLongClick(position: Int) {
        selectedEntity = showFoundItems.value!![position]
        showOptions.value = Pair(position, optionsList[currentSelectedEntity.value!!])
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    companion object {
        const val POSITION_FIND_USERS = 0
    }

    init {
        finderEntities.value = listOf(
            ListItem(
                id = "ITEM_FIND_USER",
                title = "Пользователи",
                icon = EitherResource.Id(R.drawable.ic_user)
            ),
            ListItem(
                id = "ITEM_FIND_GROUP",
                title = "Группы",
                icon = EitherResource.Id(R.drawable.ic_group)
            ),
            ListItem(
                id = "ITEM_FIND_SUBJECT",
                title = "Предметы",
                icon = EitherResource.Id(R.drawable.ic_subject)
            ),
            ListItem(
                id = "ITEM_FIND_SPECIALTY",
                title = "Специальности",
                icon = EitherResource.Id(R.drawable.ic_specialty)
            ),
            ListItem(
                id = "ITEM_FIND_COURSE",
                title = "Курсы",
                icon = EitherResource.Id(R.drawable.ic_course)
            ),
        )
        optionsList = listOf(userOptions, groupOptions, subjectOptions)

        viewModelScope.launch {
            queryByName.flatMapLatest { name: String ->
                findByTypedNameActions[currentSelectedEntity.value!!].invoke(name)
            }.collect { resource ->
                showFoundItems.value = ArrayList(
                    (resource as Resource.Success).data
                )
                foundEntities[currentSelectedEntity.value!!] = ArrayList(
                    resource.data
                )
            }
        }

        onOptionItemClickActions = mapOf(
            "OPTION_SHOW_PROFILE" to Action {
                openProfile.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_EDIT_USER" to Action {
                val selectedUser = selectedEntity as User
                val args: MutableMap<String, String> = HashMap()
                args[UserEditorActivity.USER_ROLE] = selectedUser.role
                args[UserEditorActivity.USER_ID] = selectedUser.id
                args[UserEditorActivity.USER_GROUP_ID] = selectedUser.groupId ?: ""
                openUserEditor.setValue(args)
            },
            "OPTION_DELETE_USER" to Action {
                val selectedUser = selectedEntity as User
                openConfirmation.value = Pair(
                    "Удаление пользователя",
                    "Удаленного пользователя нельзя будет восстановить"
                )
                subscribeConfirmation = RxBusConfirm.getInstance()
                    .event
                    .subscribe { confirm: Boolean ->
                        if (confirm) {
                            interactor.removeUser(selectedUser)
                                .subscribe({}) { throwable: Throwable ->
                                    if (throwable is NetworkException) {
                                        showMessage.value = "Проверьте подключение к интернету"
                                    }
                                }
                        }
                        subscribeConfirmation!!.dispose()
                    }
            },
            "OPTION_SHOW_GROUP" to Action { openGroup.setValue(selectedEntity!!.id) },
            "OPTION_EDIT_GROUP" to Action {
                openGroupEditor.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_DELETE_GROUP" to Action {
                openConfirmation.value = Pair("Удалить группу", "Вы точно уверены???")
                RxBusConfirm.getInstance()
                    .event
                    .subscribe { confirm: Boolean ->
                        if (confirm) {
                            viewModelScope.launch {
                                try {
                                    interactor.removeGroup(selectedEntity as Group)
                                } catch (e: Exception) {
                                    if (e is NetworkException) {
                                        showMessage.value = "Проверьте подключение к интернету"
                                    }
                                }
                            }
                        }
                    }
            },
            "OPTION_SHOW_SUBJECT" to Action {},
            "OPTION_EDIT_SUBJECT" to Action {
                openSubjectEditor.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_DELETE_SUBJECT" to Action {
                openConfirmation.value = Pair("Удалить предмет", "Вы точно уверены???")
                RxBusConfirm.getInstance()
                    .event
                    .subscribe { confirm: Boolean ->
                        if (confirm) {
                            viewModelScope.launch {
                                try {
                                    interactor.removeSubject(selectedEntity as Subject)
                                } catch (e: Exception) {
                                    if (e is NetworkException) {
                                        showMessage.value = "Проверьте подключение к интернету"
                                    }
                                }
                            }
                        }
                    }
            }
        )
    }
}