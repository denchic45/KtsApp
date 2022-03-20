package com.denchic45.kts.ui.adminPanel.finder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.EitherMessage
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.domain.usecase.RemoveGroupUseCase
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.confirm.ConfirmInteractor
import com.denchic45.kts.ui.userEditor.UserEditorFragment
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class FinderViewModel @Inject constructor(
    @Named("options_user") userOptions: List<ListItem>,
    @Named("options_group") groupOptions: List<ListItem>,
    @Named("options_subject") subjectOptions: List<ListItem>,
    private val interactor: FinderInteractor,
    private val removeGroupUseCase: RemoveGroupUseCase,
    private val confirmInteractor: ConfirmInteractor
) : BaseViewModel() {

    val finderEntities = MutableLiveData<List<ListItem>>()

    val currentSelectedEntity = MutableLiveData(POSITION_FIND_USERS)

    val showFoundItems = MutableLiveData<List<DomainModel>>()

    val showListEmptyState = MutableLiveData<Boolean>()

    val openGroup = SingleLiveData<String>()

    val openSubject = SingleLiveData<String>()

    val openProfile = SingleLiveData<String>()

    val openUserEditor = SingleLiveData<Map<String, String>>()

    val openSubjectEditor = SingleLiveData<String>()

    val openGroupEditor = SingleLiveData<String>()

    val openSpecialtyEditor = SingleLiveData<String>()

    val openCourse = SingleLiveData<String>()

    val showOptions = SingleLiveData<Pair<Int, List<ListItem>>>()
    private val queryByName = MutableSharedFlow<String>()
    private val onFinderItemClickActions: List<(String) -> Unit> = listOf(
        { openProfile.setValue(it) },
        { openGroup.setValue(it) },
        { openSubject.setValue(it) },
        { openSpecialtyEditor.setValue(it) },
        { navigateTo(MobileNavigationDirections.actionGlobalCourseFragment(it)) })
    private val queryTexts = mutableListOf<String?>(null, null, null, null, null)
    private val startEmptyList: List<DomainModel> = emptyList()
    private val foundEntities = mutableListOf(
        startEmptyList,
        startEmptyList,
        startEmptyList,
        startEmptyList,
        startEmptyList
    )

    private val onOptionItemClickActions: Map<String, () -> Unit>

    private val findByTypedNameActions = listOf(
        { name: String -> interactor.findUserByTypedName(name) },
        { name: String -> interactor.findGroupByTypedName(name) },
        { name: String -> interactor.findSubjectByTypedName(name) },
        { name: String -> interactor.findSpecialtyByTypedName(name) },
        { name: String -> interactor.findCourseByTypedName(name) })
    private val optionsList: List<List<ListItem>>
    private var selectedEntity: DomainModel? = null
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
        onOptionItemClickActions[optionId]!!()
    }

    fun onFinderItemClick(position: Int) {
        val item = foundEntities[currentSelectedEntity.value!!][position]
        onFinderItemClickActions[currentSelectedEntity.value!!](item.id)
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
                icon = EitherMessage.Id(R.drawable.ic_user)
            ),
            ListItem(
                id = "ITEM_FIND_GROUP",
                title = "Группы",
                icon = EitherMessage.Id(R.drawable.ic_group)
            ),
            ListItem(
                id = "ITEM_FIND_SUBJECT",
                title = "Предметы",
                icon = EitherMessage.Id(R.drawable.ic_subject)
            ),
            ListItem(
                id = "ITEM_FIND_SPECIALTY",
                title = "Специальности",
                icon = EitherMessage.Id(R.drawable.ic_specialty)
            ),
            ListItem(
                id = "ITEM_FIND_COURSE",
                title = "Курсы",
                icon = EitherMessage.Id(R.drawable.ic_course)
            ),
        )
        optionsList = listOf(userOptions, groupOptions, subjectOptions)

        viewModelScope.launch {
            queryByName.flatMapLatest { name: String ->
                findByTypedNameActions[currentSelectedEntity.value!!].invoke(name)
            }.collect { list ->
                showFoundItems.value = list
                foundEntities[currentSelectedEntity.value!!] = list
            }
        }

        onOptionItemClickActions = mapOf(
            "OPTION_SHOW_PROFILE" to {
                openProfile.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_EDIT_USER" to {
                val selectedUser = selectedEntity as User
                val args: MutableMap<String, String> = HashMap()
                args[UserEditorFragment.USER_ROLE] = selectedUser.role
                args[UserEditorFragment.USER_ID] = selectedUser.id
                args[UserEditorFragment.USER_GROUP_ID] = selectedUser.groupId ?: ""
                openUserEditor.setValue(args)
            },
            "OPTION_DELETE_USER" to {
                viewModelScope.launch {
                    val selectedUser = selectedEntity as User
                    openConfirmation(
                        Pair(
                            "Удаление пользователя",
                            "Удаленного пользователя нельзя будет восстановить"
                        )
                    )

                    if (confirmInteractor.receiveConfirm()) {
                        try {
                            interactor.removeUser(selectedUser)
                        } catch (e: Exception) {
                            if (e is NetworkException) {
                                showToast(R.string.error_check_network)
                            }
                        }
                    }
                }
            },
            "OPTION_SHOW_GROUP" to { openGroup.setValue(selectedEntity!!.id) },
            "OPTION_EDIT_GROUP" to {
                openGroupEditor.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_DELETE_GROUP" to {
                viewModelScope.launch {
                    openConfirmation(Pair("Удалить группу", "Вы точно уверены???"))
                    if (confirmInteractor.receiveConfirm()) {
                        viewModelScope.launch {
                            try {
                                removeGroupUseCase(selectedEntity!!.id)
                            } catch (e: Exception) {
                                if (e is NetworkException) {
                                    showToast(R.string.error_check_network)
                                }
                            }
                        }
                    }
                }
            },
            "OPTION_SHOW_SUBJECT" to {},
            "OPTION_EDIT_SUBJECT" to {
                openSubjectEditor.setValue(
                    selectedEntity!!.id
                )
            },
            "OPTION_DELETE_SUBJECT" to {
                viewModelScope.launch {
                    openConfirmation(Pair("Удалить предмет", "Вы точно уверены???"))
                    if (confirmInteractor.receiveConfirm()) {
                        viewModelScope.launch {
                            try {
                                interactor.removeSubject(selectedEntity as Subject)
                            } catch (e: Exception) {
                                if (e is NetworkException) {
                                    showToast(R.string.error_check_network)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}