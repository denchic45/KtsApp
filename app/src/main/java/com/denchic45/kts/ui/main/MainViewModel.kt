package com.denchic45.kts.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uipermissions.Permission
import com.denchic45.kts.uipermissions.UIPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.function.Predicate
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val interactor: MainInteractor
) : BaseViewModel() {

    private val screenIdsWithFab: Set<Int> = setOf(
        R.id.courseFragment
    )

    private val mainScreenIds: Set<Int> = setOf(R.id.menu_timetable, R.id.menu_group)
    private val onNavItemClickActions = mapOf(
        R.string.nav_tasks to { open.value = 0 },
        R.string.nav_duty_roster to { open.value = 0 },
        R.string.nav_schedule to { open.value = 0 },

        R.string.nav_control_panel to { open.value = R.id.action_global_menu_admin_panel },
        R.string.nav_settings to { open.value = R.id.action_global_menu_settings },
        R.string.nav_help to { open.value = 0 },
    )

    val fabVisibility: MutableLiveData<Boolean> = MutableLiveData()

    private var courseUuids = emptyMap<String,String>()

    val goBack = SingleLiveData<Unit>()

    val closeNavMenu = SingleLiveData<Unit>()

    val open = SingleLiveData<Int>()

    val openCourse = SingleLiveData<String>()

    val menuBtnVisibility = SingleLiveData<Pair<Int, Boolean>>()

    val toolbarNavigationState = MutableStateFlow(ToolbarNavigationState.MENU)

    enum class ToolbarNavigationState { NONE, MENU, BACK }

    val openProfile = SingleLiveData<String>()

    val userInfo = MutableLiveData<User>()

    private val uiPermissions: UIPermissions
    var selectedDate = SingleLiveData<Date>()

    var openLogin = SingleLiveData<Void>()

    val bottomMenuVisibility: MutableLiveData<Boolean> = MutableLiveData(true)

    val navMenuItems: MutableStateFlow<NavMenuState> = MutableStateFlow(NavMenuState.NavMenuEmpty)

    fun onOptionItemSelect(itemId: Int) {
        when (itemId) {
            R.id.option_select_today -> selectedDate.setValue(Date())
            android.R.id.home -> goBack.call()
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    fun onResume() {
//        menuBtnVisibility.value = Pair(
//            R.id.menu_admin_panel,
//            uiPermissions.isAllowed(ALLOW_CONTROL)
//        )
    }

    fun onProfileClick() {
        openProfile.value = userInfo.value!!.uuid
    }

    fun onNavItemClick(position: Int) {
        val name =
            ((navMenuItems.value as NavMenuState.NavMenu).items[position] as NavTextItem).name
        name.onId {
            onNavItemClickActions.getValue(it).invoke()
        }.onString {
            openCourse.postValue(courseUuids[it])
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
        viewModelScope.launch(Dispatchers.IO) { interactor.startListeners() }
        viewModelScope.launch {
            interactor.observeHasGroup().collect { hasGroup: Boolean ->
                menuBtnVisibility.value = Pair(R.id.menu_group, hasGroup)
            }
        }

        viewModelScope.launch {
            interactor.findOwnCourses()
                .combine(interactor.observeHasGroup()) { courses, hasGroup ->
                    courseUuids = courses.map { it.name to it.uuid }.toMap()
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
                .collect { optional: Optional<User> ->
                    optional.ifPresent { value: User ->
                        userInfo.setValue(value)
                    }
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

        uiPermissions = UIPermissions(interactor.findThisUser())
        uiPermissions.addPermissions(
            Permission(
                ALLOW_CONTROL,
                Predicate { (_, _, _, _, _, role, _, _, _, _, _, _, admin) -> role == User.HEAD_TEACHER || admin })
        )
    }

    sealed class NavMenuState {
        data class NavMenu(
            private val courses: List<CourseInfo>,
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
                            uuid = it.uuid,
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


}