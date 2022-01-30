package com.denchic45.kts.ui.group

import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorActivity
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.group.users.GroupUsersFragment
import com.denchic45.kts.ui.timetable.TimetableFragment
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.tabs.TabLayout
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class GroupFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GroupViewModel>
    private val viewModel: GroupViewModel by viewModels { viewModelFactory }
    private var viewPager: ViewPager? = null
    private var menu: Menu? = null
    private var navController: NavController? = null
    private var tabLayout: TabLayout? = null
    private lateinit var appBarController: AppBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        viewModel.onPrepareOptions(viewPager!!.currentItem)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.options_group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionSelect(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_group, container, false)
        viewPager = root.findViewById(R.id.vp_group)
        tabLayout = root.findViewById(R.id.tl_group)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController(view)
        lifecycleScope.launchWhenStarted {
            viewModel.title
                .filter(String::isNotEmpty)
                .collect { title: String ->
                    (requireActivity() as AppCompatActivity).supportActionBar!!.title = title
                }
        }
        viewModel.menuItemVisibility.observe(
            viewLifecycleOwner
        ) { idAndVisiblePair: Pair<Int, Boolean> ->
            val menuItem = menu!!.findItem(
                idAndVisiblePair.first
            )
            if (menuItem != null) menuItem.isVisible = idAndVisiblePair.second
        }
        viewModel.initTabs.observe(viewLifecycleOwner) { size: Int ->
            val adapter = GroupFragmentAdapter(
                childFragmentManager,
                viewModel.groupId,
                size,
                requireContext()
            )
            viewPager!!.adapter = adapter
            viewPager!!.offscreenPageLimit = 3
            viewPager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    viewModel.onPageSelect(position)
                }
            })
            tabLayout!!.setupWithViewPager(viewPager)
        }
        viewModel.openUserEditor.observe(
            viewLifecycleOwner
        ) { (userType, groupId) ->
            val intent = Intent(requireContext(), UserEditorActivity::class.java)
            intent.putExtra(UserEditorActivity.USER_ROLE, userType)
            intent.putExtra(UserEditorActivity.USER_GROUP_ID, groupId)
            startActivity(intent)
        }
        viewModel.openGroupEditor.observe(viewLifecycleOwner) { groupId: String? ->
            val intent = Intent(activity, GroupEditorActivity::class.java)
            intent.putExtra(GroupEditorFragment.GROUP_ID, groupId)
            requireActivity().startActivity(intent)
        }
        viewModel.finish.observe(
            viewLifecycleOwner
        ) { navController!!.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayout = null
        viewPager!!.adapter = null
        viewPager = null
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    companion object {
        const val GROUP_ID = "GROUP_UUID"
    }
}