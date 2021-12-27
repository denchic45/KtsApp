package com.denchic45.kts.ui.course.taskEditor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentTaskEditorBinding
import com.denchic45.kts.ui.BaseFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H

class TaskEditorFragment :
    BaseFragment<TaskEditorViewModel, FragmentTaskEditorBinding>(R.layout.fragment_task_editor) {

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            llAvailabilityDate.setOnClickListener { viewModel.onAvailabilityDateClick() }

            viewModel.titleField.observe(viewLifecycleOwner) {
                if (etName.text.toString() != it) etName.setText(it)
            }

            viewModel.descriptionField.observe(viewLifecycleOwner) {
                if (etDescription.text.toString() != it) etDescription.setText(it)
            }

            viewModel.dateField.observe(viewLifecycleOwner) {
                if (it != null) {
                    tvAvailabilityDate.text = it
                } else {
                    tvAvailabilityDate.text = "Без срока сдачи"
                }
            }

            viewModel.openDatePicker.observe(viewLifecycleOwner) {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(
                        CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())
                            .build()
                    )
                    .setSelection(it)
                    .build()

                picker.show(parentFragmentManager, null)
                picker.addOnPositiveButtonClickListener(viewModel::onAvailabilityDateSelect)
            }

            viewModel.openTimePicker.observe(viewLifecycleOwner) {
                val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(CLOCK_24H)
                    .setHour(it.first)
                    .setMinute(it.second)
                    .build()
                picker.show(parentFragmentManager, null)
                picker.addOnPositiveButtonClickListener {
                    viewModel.onAvailabilityTimeSelect(
                        picker.hour,
                        picker.minute
                    )
                }
            }
        }
    }
}