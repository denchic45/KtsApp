package com.denchic45.kts.ui.group.users

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.databinding.FragmentGroupUsersBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.UserAdapter
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.ViewUtils
import com.denchic45.kts.utils.collectWhenStarted
import com.example.appbarcontroller.appbarcontroller.AppBarController

class GroupUsersFragment :
    BaseFragment<GroupUsersViewModel, FragmentGroupUsersBinding>(R.layout.fragment_group_users) {

    override val binding by viewBinding(FragmentGroupUsersBinding::bind)
    override val viewModel: GroupUsersViewModel by viewModels { viewModelFactory }

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
        val userAdapter = UserAdapter({ position -> viewModel.onUserItemClick(position) },
            { position -> viewModel.onUserItemLongClick(position) })

        with(binding) {
            rvUsers.adapter = userAdapter
            viewModel.showUserOptions.observe(
                viewLifecycleOwner
            ) {
                val popupWindow = ListPopupWindow(requireActivity())
                popupWindow.anchorView = rvUsers.layoutManager!!.findViewByPosition(it.first)
                val adapter = ListPopupWindowAdapter(requireContext(), it.second)
                popupWindow.setAdapter(adapter)
                popupWindow.width = ViewUtils.measureAdapter(adapter, activity)
                popupWindow.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
                popupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    popupWindow.dismiss()
                    viewModel.onOptionUserClick(it.second[position].id)
                }
                popupWindow.show()
            }
        }

        viewModel.users.collectWhenStarted(lifecycleScope) {
            userAdapter.submitList(it)
        }

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
        fun newInstance(groupId: String): GroupUsersFragment {
            return GroupUsersFragment().apply {
                arguments = bundleOf(GROUP_ID to groupId)
            }
        }
    }
}