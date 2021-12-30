package com.denchic45.kts.ui.course.taskEditor

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.databinding.FragmentTaskEditorBinding
import com.denchic45.kts.databinding.ItemAttachmentBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.utils.FilePicker
import com.denchic45.kts.utils.path
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import java.io.File

class TaskEditorFragment :
    BaseFragment<TaskEditorViewModel, FragmentTaskEditorBinding>(R.layout.fragment_task_editor) {

    companion object {
        const val TASK_ID = "TASK_ID"
    }

    private val adapter = adapter { delegates(AttachmentAdapterDelegate()) }

    private lateinit var filePicker: FilePicker

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        filePicker = FilePicker(requireActivity() as AppCompatActivity, this) {
            if (it.resultCode == Activity.RESULT_OK) {
                with(it.data!!) {
                    clipData?.let { clipData ->
                        viewModel.onAttachmentsSelect(
                            List(clipData.itemCount) {
                                File(
                                    requireContext().path(
                                        clipData.getItemAt(0).uri
                                    )
                                )
                            }
                        )
                    } ?: viewModel.onAttachmentsSelect(listOf(File(requireContext().path(data!!))))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_task_editor, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            rvFiles.adapter = adapter

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

            viewModel.openFileChooser.observe(viewLifecycleOwner) {
                filePicker.selectFiles()
            }

            viewModel.showFiles.observe(viewLifecycleOwner) {
                adapter.submit(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }
}

class AttachmentAdapterDelegate : ListItemAdapterDelegate<Attachment, AttachmentHolder>() {
    override fun isForViewType(item: Any): Boolean = item is Attachment

    override fun onBindViewHolder(item: Attachment, holder: AttachmentHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): AttachmentHolder {
        return AttachmentHolder(parent.viewBinding(ItemAttachmentBinding::inflate))
    }

}

class AttachmentHolder(itemAttachmentBinding: ItemAttachmentBinding) :
    BaseViewHolder<Attachment, ItemAttachmentBinding>(itemAttachmentBinding) {
    override fun onBind(item: Attachment) {
        with(binding) {
            tvName.text = item.file.name
        }
    }

}