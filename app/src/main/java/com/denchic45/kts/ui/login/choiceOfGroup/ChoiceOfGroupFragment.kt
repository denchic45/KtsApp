package com.denchic45.kts.ui.login.choiceOfGroup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.databinding.FragmentChoiceOfGroupBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.GroupAdapter

class ChoiceOfGroupFragment :
    BaseFragment<ChoiceOfGroupViewModel, FragmentChoiceOfGroupBinding>(R.layout.fragment_choice_of_group) {

    override val viewModel: ChoiceOfGroupViewModel by viewModels { viewModelFactory }
    override val binding: FragmentChoiceOfGroupBinding by viewBinding(
        FragmentChoiceOfGroupBinding::bind
    )
    private lateinit var adapter: GroupAdapter
    private var navController: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GroupAdapter { position -> viewModel.onGroupItemClick(position) }
        with(binding) {
            adapter = GroupAdapter { position -> viewModel.onGroupItemClick(position) }
            adapter.setSpecialtyItemClickListener { position: Int ->
                viewModel.onSpecialtyItemClick(
                    position
                )
            }
            rvGroups.adapter = adapter
        }
        navController = findNavController(view)
        viewModel.groupAndSpecialtyList.observe(viewLifecycleOwner) { list: List<DomainModel> ->
            adapter.submitList(ArrayList(list))
        }
        viewModel.finish.observe(
            viewLifecycleOwner
        ) { navController!!.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvGroups.adapter = null
    }
}