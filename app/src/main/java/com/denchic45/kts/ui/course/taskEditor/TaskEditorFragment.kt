package com.denchic45.kts.ui.course.taskEditor

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
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

    companion object {
        const val TASK_ID = "TASK_ID"
    }

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            llAvailabilityDate.setOnClickListener { viewModel.onAvailabilityDateClick() }

            ivRemoveAvailabilityDate.setOnClickListener { viewModel.onRemoveAvailabilityDate() }

            viewModel.titleField.observe(viewLifecycleOwner) {
                if (etName.text.toString() != it) etName.setText(it)
            }

            viewModel.descriptionField.observe(viewLifecycleOwner) {
                if (etDescription.text.toString() != it) etDescription.setText(it)
            }

            viewModel.availabilityDateField.observe(viewLifecycleOwner) {
                val dateNotNull = it != null
                if (dateNotNull) {
                    tvAvailabilityDate.text = it
                } else {
                    tvAvailabilityDate.text = "Без срока сдачи"
                }
                ImageViewCompat.setImageTintList(
                    ivAvailabilityDate,
                    if (dateNotNull)
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.blue
                            )
                        )
                    else null
                )

                tvAvailabilityDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (dateNotNull) R.color.blue
                        else R.color.dark_gray
                    )
                )
            }

            viewModel.availabilityDateRemoveVisibility.observe(viewLifecycleOwner) {
                ivRemoveAvailabilityDate.visibility = if (it) View.VISIBLE else View.GONE
            }

            viewModel.filesVisibility.observe(viewLifecycleOwner) {
                tvHeaderFiles.visibility = if (it) View.VISIBLE else View.GONE
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