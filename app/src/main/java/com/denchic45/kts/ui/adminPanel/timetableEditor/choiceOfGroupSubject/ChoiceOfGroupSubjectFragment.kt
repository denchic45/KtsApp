package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.Resource
import com.denchic45.kts.databinding.FragmentChoiceOfGroupSubjectBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.adapter.SubjectAdapter
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.widget.ListStateLayout

class ChoiceOfGroupSubjectFragment :
    BaseFragment<GroupSubjectChooserViewModel, FragmentChoiceOfGroupSubjectBinding>(
        R.layout.fragment_choice_of_group_subject,
        R.menu.options_choice_of_group
    ) {

    override val binding: FragmentChoiceOfGroupSubjectBinding by viewBinding(
        FragmentChoiceOfGroupSubjectBinding::bind
    )

    override val viewModel: GroupSubjectChooserViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SubjectAdapter { position: Int -> viewModel.onSubjectClick(position) }
        with(binding) {
            rvSubjects.adapter = adapter
            rvSubjects.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            viewModel.openIconPicker.observe(viewLifecycleOwner) {
                IconPickerDialog().show(
                    requireActivity().supportFragmentManager, null
                )
            }
            viewModel.openSubjectChooser.observe(
                viewLifecycleOwner
            ) { navController.navigate(R.id.action_choiceOfGroupSubjectFragment_to_subjectChooserFragment) }

                viewModel.showSubjectsOfGroup.collectWhenStarted(lifecycleScope) { resource ->
                    if (resource is Resource.Success) {
                        listStateLayout.showList()
                        adapter.submitList(resource.data)
                    } else if (resource is Resource.Error) {
                        if (resource.error is NetworkException) {
                            listStateLayout.showView(ListStateLayout.NETWORK_VIEW)
                        }
                    }
                }
            }

    }

}