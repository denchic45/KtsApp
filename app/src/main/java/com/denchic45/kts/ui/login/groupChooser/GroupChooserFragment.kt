package com.denchic45.kts.ui.login.groupChooser

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentGroupChooserBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.adapter.GroupAdapter
import com.denchic45.kts.util.collectWhenStarted

class GroupChooserFragment : BaseFragment<GroupChooserViewModel, FragmentGroupChooserBinding>(
    R.layout.fragment_group_chooser
) {

    override val viewModel: GroupChooserViewModel by viewModels { viewModelFactory }
    override val binding: FragmentGroupChooserBinding by viewBinding(
        FragmentGroupChooserBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GroupAdapter { position -> viewModel.onGroupItemClick(position) }
        with(binding) {
            adapter.setSpecialtyItemClickListener { position: Int ->
                viewModel.onSpecialtyItemClick(position)
            }
            rvGroups.adapter = adapter
        }
        viewModel.groupAndSpecialtyList.collectWhenStarted(lifecycleScope, adapter::submitList)
    }

}