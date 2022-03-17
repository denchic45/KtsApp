package com.denchic45.kts.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.CustomToolbar
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ActivityMainBinding
import com.denchic45.kts.ui.BaseActivity
import com.denchic45.kts.ui.adapter.NavDropdownItemHolder
import com.denchic45.kts.ui.adapter.NavItemHolder
import com.denchic45.kts.ui.adapter.navAdapter
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.login.LoginActivity
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.updateView.SnackbarUpdateView
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.kts.utils.findFragmentContainerNavController
import com.denchic45.kts.utils.toast
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.Sets
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.collect


class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    override val viewModel: MainViewModel by viewModels { viewModelFactory }
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: CustomToolbar
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var snackbar: Snackbar

    private lateinit var bnv: BottomNavigationView
    private val navAdapter = navAdapter {
        extensions {
            clickBuilder<NavItemHolder> {
                onClick = {
                    viewModel.onNavItemClick(it)
                }
            }
            clickBuilder<NavDropdownItemHolder> {
                onClick = {
                    viewModel.onExpandCoursesClick()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)

        viewModel.setActivityForService(this)

        snackbar = Snackbar.make(this, binding.container, "", Snackbar.LENGTH_INDEFINITE)
        val customSnackView = SnackbarUpdateView(this)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(customSnackView, 0)

        customSnackView.onDownloadClickListener = { viewModel.onDownloadUpdateClick() }
        customSnackView.onLaterClickListener = { viewModel.onLaterUpdateClick() }

        bnv = findViewById(R.id.bottom_nav_view)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar_main)

//        params = toolbar.layoutParams as AppBarLayout.LayoutParams
        appBarLayout = findViewById(R.id.app_bar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        AppBarController.create(this, appBarLayout)
        setSupportActionBar(toolbar)
        setupWithNavController(navigationView, navController)
        setupWithNavController(bnv, navController)

        bnv.setOnItemReselectedListener { refreshCurrentFragment() }

        viewModel.updateBannerState.collectWhenStarted(lifecycleScope) { bannerState ->
            if (bannerState !is MainViewModel.UpdateBannerState.Hidden)
                snackbar.show()

            when (bannerState) {
                MainViewModel.UpdateBannerState.Hidden -> snackbar.dismiss()
                MainViewModel.UpdateBannerState.Remind -> {
                    customSnackView.showState(
                        SnackbarUpdateView.UpdateState.REMIND
                    )
                }
                is MainViewModel.UpdateBannerState.Loading -> {
                    customSnackView.showState(SnackbarUpdateView.UpdateState.LOADING)
                    customSnackView.updateLoadingProgress(bannerState.progress, bannerState.info)
                }
                MainViewModel.UpdateBannerState.Install -> {
                    customSnackView.showState(
                        SnackbarUpdateView.UpdateState.INSTALL
                    )
                }


            }
        }

        viewModel.menuBtnVisibility.observe(
            this
        ) { itemIdWithVisibilityPair: Pair<Int, Boolean> ->
            bnv.menu.findItem(
                itemIdWithVisibilityPair.first
            ).isVisible = itemIdWithVisibilityPair.second
        }

        with(binding) {
            rvNav.adapter = navAdapter
            viewModel.bottomMenuVisibility.observe(this@MainActivity) {
                bnv.visibility = if (it) View.VISIBLE else View.GONE
            }
            viewModel.fabVisibility.observe(this@MainActivity) {
                if (it) fabMain.show() else fabMain.hide()
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.toolbarNavigationState.collect {

                toggle = ActionBarDrawerToggle(
                    this@MainActivity,
                    drawerLayout,
                    toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
                )

                when (it) {
                    MainViewModel.ToolbarNavigationState.MENU -> {
                        drawerLayout.addDrawerListener(toggle)
                        toggle.isDrawerIndicatorEnabled = true
                        toggle.syncState()
                    }
                    MainViewModel.ToolbarNavigationState.BACK -> {
                        toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
                        toggle.isDrawerIndicatorEnabled = false
                        toggle.syncState()
                    }
                }

                toggle.toolbarNavigationClickListener = View.OnClickListener {
                    onBackPressed()
                }

            }
        }

        viewModel.userInfo.observe(this) { user: User ->
            val headerView = binding.navHeader.root
            headerView.setOnClickListener { v: View -> viewModel.onProfileClick() }
            Glide.with(this@MainActivity)
                .load(user.photoUrl)
                .into(binding.navHeader.ivAvatar)
            binding.navHeader.tvFullName.text = user.fullName
        }
        viewModel.navigate.observe(this) {
            binding.drawerLayout.close()
            navController.navigate(it)
        }

        viewModel.openCourse.observe(this) { id ->
            binding.drawerLayout.close()
            navController.navigate(
                R.id.action_global_courseFragment,
                bundleOf(CourseFragment.COURSE_ID to id)
            )
        }

        viewModel.openLogin.observe(this) {
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        viewModel.openProfile.observe(this) { id ->
            binding.drawerLayout.close()
            val bundle = Bundle()
            bundle.putString(ProfileFragment.USER_ID, id)
            drawerLayout.close()
            navController.navigate(R.id.action_global_profileFragment, bundle)
        }

        viewModel.goBack.observe(this) {
            navController.navigateUp()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.navMenuItems.collect {
                if (it is MainViewModel.NavMenuState.NavMenu) {
                    navAdapter.submit(it.items)
                }
            }
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            viewModel.onDestinationChanged(destination.id)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == AppVersionService.UPDATE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                viewModel.onUpdateDownloaded(this)
//            } else if (resultCode == RESULT_CANCELED || resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
//                viewModel.onUpdateCancelled()
//            }
//        }
//    }

    override val navController: NavController by lazy {
        findFragmentContainerNavController(R.id.nav_host_fragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionItemSelect(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        toast("39")
    }

    private fun refreshCurrentFragment() {
        val ids = Sets.newHashSet(R.id.menu_timetable, R.id.menu_group, R.id.menu_admin_panel)
        var id = navController.currentDestination!!.id
        navController.popBackStack(id, true)
        while (!ids.contains(id)) {
            id = navController.currentDestination!!.id
            navController.popBackStack(id, true)
        }
        navController.navigate(id)
    }
}