package com.denchic45.kts.ui.adminPanel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentAdminPanelBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.ItemAdapter
import com.denchic45.kts.ui.adapter.OnItemClickListener
import com.denchic45.kts.ui.creator.CreatorDialog
import com.denchic45.kts.utils.setActivityTitle
import com.example.appbarcontroller.appbarcontroller.AppBarController

class AdminPanelFragment :
    BaseFragment<AdminPanelViewModel, FragmentAdminPanelBinding>(R.layout.fragment_admin_panel) {

    override val viewModel: AdminPanelViewModel by viewModels { viewModelFactory }
    override val binding: FragmentAdminPanelBinding by viewBinding(FragmentAdminPanelBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val adapter = ItemAdapter()
            adapter.submitList(viewModel.itemList)
            adapter.itemClickListener =
                OnItemClickListener { position: Int -> viewModel.onItemClick(position) }
            rv.adapter = adapter
            viewModel.openTimetableEditor.observe(
                viewLifecycleOwner
            ) { navController.navigate(R.id.action_menu_admin_panel_to_timetableEditorFragment) }
            viewModel.openUserFinder.observe(
                viewLifecycleOwner
            ) { navController.navigate(R.id.action_menu_admin_panel_to_finderFragment2) }
            viewModel.openCreator.observe(viewLifecycleOwner) {
                val creatorDialog = CreatorDialog()
                creatorDialog.show(childFragmentManager, null)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val appBarController = AppBarController.findController(requireActivity())
        appBarController.setExpandableIfViewCanScroll(binding.rv, viewLifecycleOwner)
        appBarController.setLiftOnScroll(true)
        setActivityTitle("")
    }
}