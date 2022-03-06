package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.databinding.FragmentChoiceOfGroupSubjectBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.SubjectAdapter
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.utils.NetworkException
import com.denchic45.widget.ListStateLayout

class ChoiceOfGroupSubjectFragment :
    BaseFragment<ChoiceOfGroupSubjectViewModel, FragmentChoiceOfGroupSubjectBinding>(
        R.layout.fragment_choice_of_group_subject
    ) {

    override val binding: FragmentChoiceOfGroupSubjectBinding by viewBinding(
        FragmentChoiceOfGroupSubjectBinding::bind
    )

    override val viewModel: ChoiceOfGroupSubjectViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.options_choice_of_group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionsItemSelected(item.itemId)
        return super.onOptionsItemSelected(item)
    }

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
            viewModel.openChoiceOfSubject.observe(
                viewLifecycleOwner
            ) { findNavController().navigate(R.id.action_choiceOfGroupSubjectFragment_to_choiceOfSubjectFragment) }
            viewModel.updateIconEventSubject.observe(
                viewLifecycleOwner
            ) { adapter.notifyItemChanged(0, SubjectAdapter.PAYLOAD.UPDATE_ICON) }
            viewModel.showSubjectsOfGroup.observe(
                viewLifecycleOwner
            ) { resource: Resource<List<Subject>> ->
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