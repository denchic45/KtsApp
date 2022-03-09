package com.denchic45.kts.ui.tasks.upcoming

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentListBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.TaskAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import kotlinx.coroutines.flow.collect

class UpcomingTasksFragment :
    BaseFragment<UpcomingTasksViewModel, FragmentListBinding>(R.layout.fragment_list) {
    override val viewModel: UpcomingTasksViewModel by viewModels { viewModelFactory }
    override val binding: FragmentListBinding by viewBinding(FragmentListBinding::bind)
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_course_content, menu)
        this.menu = menu
        viewModel.onCreateOptions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val adapter = adapter {
                delegates(TaskAdapterDelegate())
            }
            rvList.adapter = adapter
            lifecycleScope.launchWhenStarted {
                viewModel.tasks.collect(adapter::submit)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menu = null
    }
}