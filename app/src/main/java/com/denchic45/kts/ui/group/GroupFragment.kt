package com.denchic45.kts.ui.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentGroupBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.group.users.GroupUsersFragment
import com.denchic45.kts.ui.timetable.TimetableFragment
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController

class GroupFragment : BaseFragment<GroupViewModel, FragmentGroupBinding>(R.layout.fragment_group) {

    override val binding: FragmentGroupBinding by viewBinding(FragmentGroupBinding::bind)
    override val viewModel: GroupViewModel by viewModels { viewModelFactory }
    private var menu: Menu? = null
    private lateinit var appBarController: AppBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        viewModel.onPrepareOptions(binding.vp.currentItem)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.options_group, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.menuItemVisibility.observe(
            viewLifecycleOwner
        ) { idAndVisiblePair: Pair<Int, Boolean> ->
            val menuItem = menu!!.findItem(
                idAndVisiblePair.first
            )
            if (menuItem != null) menuItem.isVisible = idAndVisiblePair.second
        }
        viewModel.openUserEditor.observe(
            viewLifecycleOwner
        ) { (userType, groupId) ->
            val intent = Intent(requireContext(), UserEditorActivity::class.java)
            intent.putExtra(UserEditorActivity.USER_ROLE, userType)
            intent.putExtra(UserEditorActivity.USER_GROUP_ID, groupId)
            startActivity(intent)
        }
        viewModel.openGroupEditor.observe(viewLifecycleOwner) { groupId ->
            navController.navigate(
                R.id.action_global_groupEditorFragment,
                bundleOf(GroupEditorFragment.GROUP_ID to groupId)
            )
        }
        with(binding) {
            viewModel.initTabs.observe(viewLifecycleOwner) { size: Int ->
                val adapter = GroupFragmentAdapter(
                    childFragmentManager,
                    viewModel.groupId,
                    size,
                    requireContext()
                )
                vp.adapter = adapter
                vp.offscreenPageLimit = 3
                vp.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        viewModel.onPageSelect(position)
                    }
                })
                tl.setupWithViewPager(vp)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appBarController = AppBarController.findController(requireActivity())
        appBarController.setLiftOnScroll(false)
    }

    class GroupFragmentAdapter(
        fm: FragmentManager,
        private val groupId: String,
        private val size: Int,
        private val context: Context
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> context.getString(R.string.group_users)
                1 -> context.getString(R.string.group_courses)
                2 -> context.getString(R.string.timetable)
                else -> throw IllegalStateException()
            }
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> GroupUsersFragment.newInstance(groupId)
                1 -> GroupCoursesFragment.newInstance(groupId)
                2 -> TimetableFragment.newInstance(groupId)
                else -> throw IllegalStateException()
            }
        }

        override fun getCount(): Int {
            return size
        }
    }

    companion object {
        const val GROUP_ID = "Group GROUP_ID"
    }
}