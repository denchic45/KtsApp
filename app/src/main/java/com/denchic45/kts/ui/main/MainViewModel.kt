package com.denchic45.kts.ui.main

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.appVersion.GoogleAppVersionService
import com.denchic45.kts.MobileNavigationDirections
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.ui.UiText
import com.denchic45.kts.data.model.ui.onId
import com.denchic45.kts.data.model.ui.onString
import com.denchic45.kts.ui.NavigationCommand
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.adminPanel.AdminPanelFragmentDirections
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.course.CourseFragmentDirections
import com.denchic45.kts.ui.settings.SettingsFragmentDirections
import com.denchic45.kts.ui.tasks.TasksFragmentDirections
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UiPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val interactor: MainInteractor,
    private val appVersionService: GoogleAppVersionService
) : BaseViewModel() {

    private val screenIdsWithFab: Set<Int> = setOf(
        R.id.courseFragment,
        R.id.groupEditorFragment
    )

    val updateBannerState = MutableStateFlow<UpdateBannerState>(UpdateBannerState.Hidden)

    fun setActivityForService(activity: Activity) {
        appVersionService.activityRef = WeakReference(activity)
    }

    private val mainScreenIds: Set<Int> = setOf(R.id.menu_timetable, R.id.menu_group)
    private val onNavItemClickActions = mapOf(
        R.string.nav_tasks to {
            navigateTo(TasksFragmentDirections.actionGlobalTasksFragment())
        },
        R.string.nav_duty_roster to { },
        R.string.nav_schedule to { },

        R.string.nav_control_panel to {
            navigateTo(AdminPanelFragmentDirections.actionGlobalMenuAdminPanel())
        },
        R.string.nav_settings to { navigateTo(SettingsFragmentDirections.actionGlobalMenuSettings()) },
        R.string.nav_help to { },
    )

    val fabVisibility: MutableLiveData<Boolean> = MutableLiveData()

    private var courseIds = emptyMap<String, String>()

    val goBack = SingleLiveData<Unit>()

    val closeNavMenu = SingleLiveData<Unit>()

    val menuBtnVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val toolbarNavigationState = MutableStateFlow(ToolbarNavigationState.MENU)

    enum class ToolbarNavigationState { NONE, MENU, BACK }

    val userInfo = interactor.observeThisUser().filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, User.createEmpty())

    private val uiPermissions: UiPermissions

    var openLogin = SingleLiveData<Void>()

    val bottomMenuVisibility: MutableLiveData<Boolean> = MutableLiveData(true)

    val navMenuItems: MutableStateFlow<NavMenuState> = MutableStateFlow(NavMenuState.NavMenuEmpty)

    fun onOptionItemSelect(itemId: Int) {
        when (itemId) {
            android.R.id.home -> goBack.call()
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onResume() {
        appVersionService.observeDownloadedUpdate()
    }

    fun onProfileClick() {
        navigateTo(MobileNavigationDirections.actionGlobalProfileFragment(userInfo.value.id))
    }

    fun onNavItemClick(position: Int) {

        val name =
            ((navMenuItems.value as NavMenuState.NavMenu).items[position] as NavTextItem).name
        name.onId {
            onNavItemClickActions.getValue(it).invoke()
        }.onString {
            viewModelScope.launch {
                navigate.emit(
                    NavigationCommand.To(
                        CourseFragmentDirections.actionGlobalCourseFragment(
                            courseIds[it]!!
                        )
                    )
                )

            }
        }
    }

    fun onExpandCoursesClick() {
        val item = navMenuItems.value as NavMenuState.NavMenu
        navMenuItems.value = item.copy(expandAllCourse = !item.expandAllCourse)
    }

    fun onDestinationChanged(id: Int) {
        if (mainScreenIds.contains(id)) {
            toolbarNavigationState.value = ToolbarNavigationState.MENU
            if (!bottomMenuVisibility.value!!) {
                bottomMenuVisibility.value = true
            }
        } else if (bottomMenuVisibility.value!!) {
            toolbarNavigationState.value = ToolbarNavigationState.BACK
            bottomMenuVisibility.value = false
        }

        fabVisibility.postValue(screenIdsWithFab.contains(id))
    }

    companion object {
        private const val ALLOW_CONTROL = "ALLOW_CONTROL"
    }

    init {
        addCloseable(appVersionService)
        appVersionService.onUpdateDownloaded = {
            Log.d("lol", "startUpdate: toast DOWNLOADED")
            updateBannerState.value = UpdateBannerState.Install
        }

        appVersionService.onUpdateLoading = { progress, megabyteTotal ->
            updateBannerState.value =
                UpdateBannerState.Loading(progress, "$progress% из $megabyteTotal МБ")
        }

        appVersionService.observeUpdates(
            onUpdateAvailable = {
                updateBannerState.value = UpdateBannerState.Remind
            },
            onError = {
//                showToast("Ошибка")
//                it.printStackTrace()
//                showSnackBar(it.message ?: "Err...")
            }
        )

        viewModelScope.launch(Dispatchers.IO) { interactor.startListeners() }

        viewModelScope.launch {
            interactor.observeHasGroup().collect { hasGroup: Boolean ->
                menuBtnVisibility.value = Pair(R.id.menu_group, hasGroup)
            }
        }

        viewModelScope.launch {
            combine(
                interactor.findOwnCourses(),
                interactor.observeHasGroup()
            ) { courses, hasGroup ->
                Log.d("lol", "courses, hasGroup: $courses, $hasGroup")
                courseIds = courses.associate { it.name to it.id }
                NavMenuState.NavMenu(
                    courses,
                    interactor.findThisUser(),
                    hasGroup
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                NavMenuState.NavMenuEmpty
            ).collect {
                navMenuItems.value = it
            }
        }

        viewModelScope.launch {
            interactor.listenAuthState
                .collect { logged: Boolean ->
                    if (!logged) {
                        openLogin.call()
                    }
                }
        }

        uiPermissions = UiPermissions(interactor.findThisUser())
        uiPermissions.putPermissions(Permission(ALLOW_CONTROL, { hasAdminPerms() }))
    }

    fun onDownloadUpdateClick() {
        appVersionService.startDownloadUpdate()
        updateBannerState.value = UpdateBannerState.WaitLoading
    }

    fun onLaterUpdateClick() {
        updateBannerState.value = UpdateBannerState.Hidden
    }

    fun onInstallClick() {
        appVersionService.installUpdate()
    }

    sealed class NavMenuState {
        data class NavMenu(
            private val courses: List<CourseHeader>,
            private val user: User,
            private val hasGroup: Boolean,
            val expandAllCourse: Boolean = false
        ) : NavMenuState() {
            private val mainTextItems: MutableList<NavTextItem> = mutableListOf()
            private val footerTextItems: MutableList<NavTextItem> = mutableListOf()

            val items: List<NavItem>

            private fun create(): MutableList<NavItem> {
                val items: MutableList<NavItem> = mainTextItems.toMutableList()
                items.add(DividerItem())
                if (courses.isNotEmpty()) {
                    val nameOfDropdownCoursesNavItem: Int
                    items.add(NavSubHeaderItem(UiText.IdText(R.string.nav_courses_my)))
                    val visibleCourses = if (expandAllCourse) {
                        nameOfDropdownCoursesNavItem = R.string.nav_courses_hide
                        courses
                    } else {
                        nameOfDropdownCoursesNavItem = R.string.nav_courses_show_all
                        courses.take(5)
                    }
                    items.addAll(visibleCourses.map {
                        NavTextItem(
                            UiText.StringText(it.name),
                            id = it.id,
                            iconType = NavTextItem.IconType.CIRCLE,
                            color = UiText.StringText(it.subject.colorName)
                        )
                    })
                    if (courses.size > 5)
                        items.add(
                            NavDropdownItem(
                                UiText.IdText(nameOfDropdownCoursesNavItem),
                                expandAllCourse
                            )
                        )
                    items.add(
                        NavTextItem(
                            UiText.IdText(R.string.nav_courses_archive),
                            UiText.IdText(R.drawable.ic_archive),
                            enable = false
                        )
                    )
                    items.add(DividerItem())
                }
                items.addAll(footerTextItems)
                return items
            }

            init {
                with(mainTextItems) {
                    if (user.isStudent)
                        mainTextItems.add(
                            NavTextItem(
                                UiText.IdText(R.string.nav_tasks),
                                UiText.IdText(R.drawable.ic_tasks)
                            )
                        )
                    if (hasGroup) {
                        mainTextItems.add(
                            NavTextItem(
                                UiText.IdText(R.string.nav_duty_roster),
                                UiText.IdText(R.drawable.ic_clean),
                                enable = false
                            )
                        )
                    }
                    add(
                        NavTextItem(
                            UiText.IdText(R.string.nav_schedule),
                            UiText.IdText(R.drawable.ic_time),
                            enable = false
                        )
                    )
                }
                with(footerTextItems) {
                    if (user.admin) {
                        add(
                            NavTextItem(
                                UiText.IdText(R.string.nav_control_panel),
                                UiText.IdText(R.drawable.ic_control_panel)
                            )
                        )
                    }
                    addAll(
                        listOf(
                            NavTextItem(
                                UiText.IdText(R.string.nav_settings),
                                UiText.IdText(R.drawable.ic_settings)
                            ), NavTextItem(
                                UiText.IdText(R.string.nav_help),
                                UiText.IdText(R.drawable.ic_help),
                                enable = false
                            )
                        )
                    )
                }
                items = create()
            }
        }

        object NavMenuEmpty : NavMenuState()
    }

    sealed class UpdateBannerState {

        object Hidden : UpdateBannerState()

        object Remind : UpdateBannerState()

        object WaitLoading : UpdateBannerState()

        data class Loading(
            val progress: Long,
            val info: String
        ) : UpdateBannerState()

        object Install : UpdateBannerState()
    }


}