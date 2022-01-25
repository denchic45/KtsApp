package com.denchic45.kts.ui.group.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentGroupUsersBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.UserAdapter
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.ViewUtils
import com.example.appbarcontroller.appbarcontroller.AppBarController
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GroupUsersFragment : Fragment(R.layout.fragment_group_users) {
    private val viewBinding by viewBinding(FragmentGroupUsersBinding::bind)
    private var adapter: UserAdapter? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GroupUsersViewModel>
    private val viewModel: GroupUsersViewModel by viewModels { viewModelFactory }
    private var navController: NavController? = null


    override fun onResume() {
        super.onResume()
        view?.let {
            AppBarController.findController(requireActivity())
                .setExpandableIfViewCanScroll(viewBinding.rvUsers, viewLifecycleOwner)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = requireActivity().supportFragmentManager.primaryNavigationFragment
        with(viewBinding) {
            adapter = UserAdapter({ position -> viewModel.onUserItemClick(position) },
                { position -> viewModel.onUserItemLongClick(position) })
            rvUsers.adapter = adapter
            viewModel.showUserOptions.observe(
                viewLifecycleOwner,
                {
                    val popupWindow = ListPopupWindow(
                        requireActivity()
                    )
                    popupWindow.anchorView = rvUsers.layoutManager!!.findViewByPosition(
                        it.first
                    )
                    val adapter = ListPopupWindowAdapter(requireContext(), it.second)
                    popupWindow.setAdapter(adapter)
                    popupWindow.width = ViewUtils.measureAdapter(adapter, activity)
                    popupWindow.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
                    popupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                        popupWindow.dismiss()
                        viewModel.onOptionUserClick(it.second[position].id)
                    }
                    popupWindow.show()
                })
        }
        navController = findNavController(navHostFragment!!.requireView())
        viewModel.onGroupIdReceived(requireArguments().getString(GROUP_UUID))
        viewModel.users!!.observe(
            viewLifecycleOwner,
            { users: List<DomainModel?> -> adapter!!.submitList(users) })
        viewModel.openChoiceOfCurator.observe(
            viewLifecycleOwner,
            { navController!!.navigate(R.id.action_menu_group_to_choiceOfCuratorFragment) })

        viewModel.openUserEditor.observe(
            viewLifecycleOwner,
            { args: Map<String, String> ->
                val intent = Intent(
                    activity, UserEditorActivity::class.java
                )
                args.forEach { (name: String, value: String) -> intent.putExtra(name, value) }
                startActivity(intent)
            })
        viewModel.openProfile.observe(viewLifecycleOwner, { userId: String ->
            val bundle = Bundle()
            bundle.putString(ProfileFragment.USER_ID, userId)
            navController!!.navigate(R.id.action_global_profileFragment, bundle)
        })
        viewModel.showMessageRes.observe(viewLifecycleOwner, { resId: Int ->
            Toast.makeText(
                context, resources.getString(
                    resId
                ), Toast.LENGTH_SHORT
            ).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.rvUsers.adapter = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    companion object {
        private const val GROUP_UUID = "GROUP_UUID"

        @JvmStatic
        fun newInstance(groupId: String?): GroupUsersFragment {
            val fragment = GroupUsersFragment()
            val args = Bundle()
            args.putString(GROUP_UUID, groupId)
            fragment.arguments = args
            return fragment
        }
    }
}