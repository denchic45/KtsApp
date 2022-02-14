package com.denchic45.kts.ui.tasks.completed

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentListBinding
import com.denchic45.kts.ui.BaseFragment

class CompletedTasksFragment :
    BaseFragment<CompletedTasksViewModel, FragmentListBinding>(R.layout.fragment_list) {
    override val viewModel: CompletedTasksViewModel by viewModels { viewModelFactory }
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

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menu = null
    }
}