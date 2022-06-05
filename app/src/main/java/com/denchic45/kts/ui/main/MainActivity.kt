package com.denchic45.kts.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.CustomToolbar
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ActivityMainBinding
import com.denchic45.kts.ui.base.BaseActivity
import com.denchic45.kts.ui.adapter.NavDropdownItemHolder
import com.denchic45.kts.ui.adapter.NavItemHolder
import com.denchic45.kts.ui.adapter.navAdapter
import com.denchic45.kts.ui.login.LoginActivity
import com.denchic45.kts.ui.updateView.SnackbarUpdateView
import com.denchic45.kts.utils.collectWhenResumed
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.kts.utils.findFragmentContainerNavController
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.debounce


class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>(R.layout.activity_main) {

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
                    binding.drawerLayout.close()
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

        viewModel.setActivityForService(this)

        snackbar = Snackbar.make(this, binding.container, "", Snackbar.LENGTH_INDEFINITE)
        val snackbarUpdateView = SnackbarUpdateView(this)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(snackbarUpdateView, 0)

        snackbarUpdateView.onDownloadClickListener = { viewModel.onDownloadUpdateClick() }
        snackbarUpdateView.onLaterClickListener = { viewModel.onLaterUpdateClick() }
        snackbarUpdateView.onInstallClickListener = { viewModel.onInstallClick() }

        bnv = findViewById(R.id.bottom_nav_view)
        toolbar = findViewById(R.id.toolbar_main)

        appBarLayout = findViewById(R.id.app_bar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        AppBarController.create(this, appBarLayout)
        setSupportActionBar(toolbar)
        setupWithNavController(navigationView, navController)
        setupWithNavController(bnv, navController)

        bnv.setOnItemReselectedListener { refreshCurrentFragment() }

        viewModel.updateBannerState.debounce(1000)
            .collectWhenResumed(lifecycleScope) { bannerState ->
                if (bannerState !is MainViewModel.UpdateBannerState.Hidden)
                    snackbar.show()
                else {
                    snackbar.dismiss()
                    return@collectWhenResumed
                }

                when (bannerState) {
                    MainViewModel.UpdateBannerState.Hidden -> snackbar.dismiss()
                    MainViewModel.UpdateBannerState.Remind -> {
                        snackbarUpdateView.showState(
                            SnackbarUpdateView.UpdateState.REMIND
                        )
                    }
                    is MainViewModel.UpdateBannerState.WaitLoading -> {
                        snackbarUpdateView.showState(SnackbarUpdateView.UpdateState.LOADING)
                        snackbarUpdateView.indeterminate(true)
                    }
                    is MainViewModel.UpdateBannerState.Loading -> {
                        snackbarUpdateView.indeterminate(false)
                        snackbarUpdateView.showState(SnackbarUpdateView.UpdateState.LOADING)
                        snackbarUpdateView.updateLoadingProgress(
                            bannerState.progress,
                            bannerState.info
                        )
                    }
                    MainViewModel.UpdateBannerState.Install -> {
                        snackbarUpdateView.showState(
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
            viewModel.fabVisibility.collectWhenStarted(lifecycleScope) {
                if (it) fabMain.show() else fabMain.hide()
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

            viewModel.userInfo.collectWhenStarted(lifecycleScope) { user: User ->
                val headerView = binding.navHeader.root
                headerView.setOnClickListener {
                    binding.drawerLayout.close()
                    viewModel.onProfileClick()
                }
                Glide.with(this@MainActivity)
                    .load(user.photoUrl)
                    .into(binding.navHeader.ivAvatar)
                binding.navHeader.tvFullName.text = user.fullName
            }

            viewModel.navMenuItems.collectWhenStarted(lifecycleScope) {
                if (it is MainViewModel.NavMenuState.NavMenu) {
                    navAdapter.submit(it.items)
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                viewModel.onDestinationChanged(destination.id)
            }
        }

        viewModel.openLogin.observe(this) {
            finish()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        viewModel.goBack.observe(this) {
            navController.navigateUp()
        }
    }

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
//        toast(BuildConfig.VERSION_CODE.toString())
    }

    private fun refreshCurrentFragment() {
        val ids = setOf(R.id.menu_timetable, R.id.menu_group)
        var id = navController.currentDestination!!.id
        navController.popBackStack(id, true)
        while (!ids.contains(id)) {
            id = navController.currentDestination!!.id
            navController.popBackStack(id, true)
        }
        navController.navigate(id)
    }
}