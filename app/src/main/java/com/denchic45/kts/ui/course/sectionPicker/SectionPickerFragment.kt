package com.denchic45.kts.ui.course.sectionPicker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.denchic45.kts.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection

class SectionPickerFragment : DialogFragment(R.layout.fragment_section_picker) {
    //    @Inject
//    lateinit var viewModelFactory: ViewModelFactory<SectionPickerViewModel>
    private val viewModel: SectionPickerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val adapter by lazy {
        ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_single_choice
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setSingleChoiceItems(
                adapter,
                0
            ) { _, which -> viewModel.onSectionItemClick(which) }
            .setNegativeButton("Отмена") { dialog, which ->
                dismiss()
            }
            .setPositiveButton("ОК") { dialog, which ->
                viewModel.onSaveClick()
                dismiss()
            }
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sections.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)
        }

        viewModel.selectedSectionPosition.observe(viewLifecycleOwner) {
            (dialog as AlertDialog).listView.apply {
                if (!isItemChecked(it))
                    setItemChecked(it, true)
            }
        }
    }

    companion object {

        const val COURSE_ID = "SECTION_PICKER_COURSE_ID"

        fun newInstance(courseId: String) =
            SectionPickerFragment().apply {
                arguments = bundleOf(COURSE_ID to courseId)
            }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}