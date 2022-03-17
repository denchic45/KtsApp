package com.denchic45.kts.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.AppVersionService
import com.denchic45.kts.CustomToolbar
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.ActivityMainBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.BaseActivity
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.login.LoginActivity
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.utils.findFragmentContainerNavController
import com.denchic45.kts.utils.toast
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.install.model.ActivityResult
import com.google.common.collect.Sets
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.collect
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    override val viewModel: MainViewModel by viewModels { viewModelFactory }
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: CustomToolbar
    private lateinit var toggle: ActionBarDrawerToggle

    //    private var params: AppBarLayout.LayoutParams? = null
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

        viewModel.activityRef = WeakReference(this)

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

        bnv.setOnItemReselectedListener { item: MenuItem? -> refreshCurrentFragment() }
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
        viewModel.open.observe(this) {
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
        toast("34")
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.activityRef.clear()
    }
}