package com.denchic45.kts.ui.group.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.OptionsPopupAdapter
import com.denchic45.kts.databinding.FragmentGroupMembersBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.HeaderAdapterDelegate
import com.denchic45.kts.ui.adapter.UserAdapterDelegate
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.ViewUtils
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.click
import com.example.appbarcontroller.appbarcontroller.AppBarController

class GroupMembersFragment :
    BaseFragment<GroupMembersViewModel, FragmentGroupMembersBinding>(R.layout.fragment_group_members) {

    override val binding by viewBinding(FragmentGroupMembersBinding::bind)
    override val viewModel: GroupMembersViewModel by viewModels { viewModelFactory }

    override fun onResume() {
        super.onResume()
        view?.let {
            AppBarController.findController(requireActivity())
                .setExpandableIfViewCanScroll(binding.rvUsers, viewLifecycleOwner)
        }
    }

    override val navController: NavController by lazy {
        Navigation.findNavController(
            requireActivity().supportFragmentManager.primaryNavigationFragment!!.requireView()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userAdapter = adapter {
            delegates(HeaderAdapterDelegate(), UserAdapterDelegate())
            extensions {
                click<UserAdapterDelegate.UserHolder>(
                    onClick = { position -> viewModel.onUserItemClick(position) },
                    onLongClick = { position ->
                        viewModel.onUserItemLongClick(position)
                        true
                    }
                )
            }
        }

        with(binding) {
            rvUsers.adapter = userAdapter
            viewModel.showUserOptions.observe(
                viewLifecycleOwner
            ) {
                val popupWindow = ListPopupWindow(requireActivity())
                popupWindow.anchorView = rvUsers.layoutManager!!.findViewByPosition(it.first)
                val adapter = OptionsPopupAdapter(requireContext(), it.second)
                popupWindow.setAdapter(adapter)
                popupWindow.width = ViewUtils.measureAdapter(adapter, requireContext())
                popupWindow.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
                popupWindow.setOnItemClickListener { _, _, position, _ ->
                    popupWindow.dismiss()
                    viewModel.onOptionUserClick(it.second[position].id)
                }
                popupWindow.show()
            }
        }

        viewModel.members.collectWhenStarted(lifecycleScope, userAdapter::submit)

        viewModel.openUserEditor.observe(
            viewLifecycleOwner
        ) { args: Map<String, String> ->
            navController.navigate(R.id.action_global_userEditorFragment,
                Bundle(2).apply {
                    args.forEach { (name: String, value: String) ->
                        putString(name, value)
                    }
                })

        }
    }


    companion object {
        const val GROUP_ID = "GroupUsers GROUP_ID"
        fun newInstance(groupId: String): GroupMembersFragment {
            return GroupMembersFragment().apply {
                arguments = bundleOf(GROUP_ID to groupId)
            }
        }
    }
}