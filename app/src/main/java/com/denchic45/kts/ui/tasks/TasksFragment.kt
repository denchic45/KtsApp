package com.denchic45.kts.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentTasksBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.tasks.completed.CompletedTasksFragment
import com.denchic45.kts.ui.tasks.overdue.OverdueTasksFragment
import com.denchic45.kts.ui.tasks.upcoming.UpcomingTasksFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TasksFragment :
    BaseFragment<TasksViewModel, FragmentTasksBinding>(R.layout.fragment_tasks) {
    override val viewModel: TasksViewModel by viewModels { viewModelFactory }
    override val binding: FragmentTasksBinding by viewBinding(FragmentTasksBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val adapter = TasksFragmentAdapter(this@TasksFragment)
            vp.adapter = adapter
            TabLayoutMediator(tl, vp) { tab: TabLayout.Tab, position: Int ->
                tab.text = viewModel.tabNames[position]
            }.attach()
        }
    }

    class TasksFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UpcomingTasksFragment()
                1 -> OverdueTasksFragment()
                2 -> CompletedTasksFragment()
                else -> throw IllegalStateException()
            }
        }

    }
}