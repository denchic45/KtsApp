package com.denchic45.kts.ui.main

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.appVersion.AppVersionService
import com.denchic45.appVersion.FakeAppVersionService
import com.denchic45.appVersion.GoogleAppVersionService
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.base.BaseViewModel
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
        //todo раскммент
        appVersionService.activityRef = WeakReference(activity)
    }

    private val mainScreenIds: Set<Int> = setOf(R.id.menu_timetable, R.id.menu_group)
    private val onNavItemClickActions = mapOf(
        R.string.nav_tasks to { navigate.value = R.id.action_global_tasksFragment },
        R.string.nav_duty_roster to { navigate.value = 0 },
        R.string.nav_schedule to { navigate.value = 0 },

        R.string.nav_control_panel to { navigate.value = R.id.action_global_menu_admin_panel },
        R.string.nav_settings to { navigate.value = R.id.action_global_menu_settings },
        R.string.nav_help to { navigate.value = 0 },
    )

    val fabVisibility: MutableLiveData<Boolean> = MutableLiveData()

    private var courseIds = emptyMap<String, String>()

    val goBack = SingleLiveData<Unit>()

    val closeNavMenu = SingleLiveData<Unit>()

    val navigate = SingleLiveData<Int>()

    val openCourse = SingleLiveData<String>()

    val menuBtnVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val toolbarNavigationState = MutableStateFlow(ToolbarNavigationState.MENU)

    enum class ToolbarNavigationState { NONE, MENU, BACK }

    val openProfile = SingleLiveData<String>()

    val userInfo = MutableLiveData<User>()

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
        appVersionService.close() // TODO раскоммент
        interactor.removeListeners()
    }

    fun onResume() {
        appVersionService.observeDownloadedUpdate()
//        menuBtnVisibility.value = Pair(
//            R.id.menu_admin_panel,
//            uiPermissions.isAllowed(ALLOW_CONTROL)
//        )
    }

    fun onProfileClick() {
        openProfile.value = userInfo.value!!.id
    }

    fun onNavItemClick(position: Int) {
        val name =
            ((navMenuItems.value as NavMenuState.NavMenu).items[position] as NavTextItem).name
        name.onId {
            onNavItemClickActions.getValue(it).invoke()
        }.onString {
            openCourse.postValue(courseIds[it])
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

    fun onUpdateDownloaded() {

    }

    fun onUpdateCancelled() {}

    companion object {
        private const val ALLOW_CONTROL = "ALLOW_CONTROL"
    }

    init {
        appVersionService.onUpdateDownloaded = {
            Log.d("lol", "startUpdate: toast DOWNLOADED")
            updateBannerState.value = UpdateBannerState.Install
        }

        appVersionService.onUpdateLoading = { progress, megabyteTotal ->
            updateBannerState.value = UpdateBannerState.Loading(progress, "$progress% из $megabyteTotal МБ")
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
            interactor.findOwnCourses()
                .combine(interactor.observeHasGroup()) { courses, hasGroup ->
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
            interactor.listenThisUser()
                .collect { user -> user?.let(userInfo::setValue) }
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

            private fun generate(): MutableList<NavItem> {
                val list: MutableList<NavItem> = mainTextItems.toMutableList()
                list.add(DividerItem())
                if (courses.isNotEmpty()) {
                    val nameOfDropdownCoursesNavItem: Int
                    list.add(NavSubHeaderItem(EitherResource.Id(R.string.nav_courses_my)))
                    val visibleCourses = if (expandAllCourse) {
                        nameOfDropdownCoursesNavItem = R.string.nav_courses_hide
                        courses
                    } else {
                        nameOfDropdownCoursesNavItem = R.string.nav_courses_show_all
                        courses.take(5)
                    }
                    list.addAll(visibleCourses.map {
                        NavTextItem(
                            EitherResource.String(it.name),
                            id = it.id,
                            iconType = NavTextItem.IconType.CIRCLE,
                            color = EitherResource.String(it.subject.colorName)
                        )
                    })
                    if (courses.size > 5)
                        list.add(
                            NavDropdownItem(
                                EitherResource.Id(nameOfDropdownCoursesNavItem),
                                expandAllCourse
                            )
                        )
                    list.add(
                        NavTextItem(
                            EitherResource.Id(R.string.nav_courses_archive),
                            EitherResource.Id(R.drawable.ic_archive)
                        )
                    )
                    list.add(DividerItem())
                }
                list.addAll(footerTextItems)
                return list
            }

            init {
                with(mainTextItems) {
                    if (user.isTeacher || user.isStudent)
                        mainTextItems.add(
                            NavTextItem(
                                EitherResource.Id(R.string.nav_tasks),
                                EitherResource.Id(R.drawable.ic_tasks)
                            )
                        )
                    if (hasGroup) {
                        mainTextItems.add(
                            NavTextItem(
                                EitherResource.Id(R.string.nav_duty_roster),
                                EitherResource.Id(R.drawable.ic_clean)
                            )
                        )
                    }
                    add(
                        NavTextItem(
                            EitherResource.Id(R.string.nav_schedule),
                            EitherResource.Id(R.drawable.ic_time)
                        )
                    )
                }
                with(footerTextItems) {
                    if (user.admin) {
                        add(
                            NavTextItem(
                                EitherResource.Id(R.string.nav_control_panel),
                                EitherResource.Id(R.drawable.ic_control_panel)
                            )
                        )
                    }
                    addAll(
                        listOf(
                            NavTextItem(
                                EitherResource.Id(R.string.nav_settings),
                                EitherResource.Id(R.drawable.ic_settings)
                            ), NavTextItem(
                                EitherResource.Id(R.string.nav_help),
                                EitherResource.Id(R.drawable.ic_help)
                            )
                        )
                    )
                }
                items = generate()
            }
        }

        object NavMenuEmpty : NavMenuState()
    }

    sealed class UpdateBannerState {

        object Hidden : UpdateBannerState()

        object Remind : UpdateBannerState()

        data class Loading(
            val progress: Long,
            val info: String
        ) : UpdateBannerState()

        object Install : UpdateBannerState()
    }


}