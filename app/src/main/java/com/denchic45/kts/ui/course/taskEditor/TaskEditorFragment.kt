package com.denchic45.kts.ui.course.taskEditor

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.databinding.FragmentTaskEditorBinding
import com.denchic45.kts.databinding.ItemAttachmentBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.utils.*
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.click
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import com.jakewharton.rxbinding4.widget.textChanges
import kotlinx.coroutines.flow.collect
import java.io.File


class TaskEditorFragment :
    BaseFragment<TaskEditorViewModel, FragmentTaskEditorBinding>(R.layout.fragment_task_editor) {

    companion object {
        const val TASK_ID = "TASK_ID"
    }

    private val adapter = adapter {
        delegates(AttachmentAdapterDelegate())
        extensions {
            click<AttachmentHolder> {
                view = { it.binding.ivFileRemove }
                onClick = { position ->
                    viewModel.onRemoveFileClick(position)
                }
            }
            click<AttachmentHolder> {
                onClick = { position: Int -> viewModel.onAttachmentClick(position) }
            }
        }
    }

    private val arrayAdapter by lazy {
        object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_content,
            arrayOf("Балльная", "Бинарная", "По категориям")
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view1 = super.getView(position, convertView, parent)
                view1.isEnabled = position != 2

                return view1
            }

            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(p0: CharSequence?): FilterResults {
                        return FilterResults()
                    }

                    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                    }

                }
            }
        }
    }

    private lateinit var filePicker: FilePicker

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        filePicker = FilePicker(requireActivity() as AppCompatActivity, this, {
            if (it.resultCode == Activity.RESULT_OK) {
                with(it.data!!) {
                    clipData?.let { clipData ->
                        viewModel.onAttachmentsSelect(
                            List(clipData.itemCount) { position ->
                                File(
                                    requireContext().path(
                                        clipData.getItemAt(position).uri
                                    )
                                )
                            }
                        )
                    } ?: viewModel.onAttachmentsSelect(listOf(File(requireContext().path(data!!))))
                }
            }
        }, true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_task_editor, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            rvFiles.adapter = adapter

            etName.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onNameType)

            etDescription.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onDescriptionType)

            etCharsLimit.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onCharsLimitType)

            etAttachmentsLimit.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onAttachmentsLimitType)

            etAttachmentsSizeLimit.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onAttachmentsSizeLimitType)

            etMaxScore.textChanges()
                .compose(EditTextTransformer())
                .subscribe(viewModel::onMaxScoreType)

            etCharsLimit.filters = arrayOf(ValueFilter(0, 2000), InputFilter.LengthFilter(4))

            etAttachmentsLimit.filters = arrayOf(ValueFilter(0, 99), InputFilter.LengthFilter(2))

            etAttachmentsSizeLimit.filters =
                arrayOf(ValueFilter(0, 999), InputFilter.LengthFilter(3))

            actMarkType.setAdapter(arrayAdapter)

            actMarkType.setOnItemClickListener { _, _, position, _ ->
                viewModel.onMarkTypeSelect(position)
            }

            llAvailabilityDate.setOnClickListener { viewModel.onAvailabilityDateClick() }

            ivRemoveAvailabilityDate.setOnClickListener { viewModel.onRemoveAvailabilityDate() }

            cbAvailabilitySend.setOnCheckedChangeListener { _, check ->
                viewModel.onAvailabilitySendCheck(check)
            }

            swText.setOnCheckedChangeListener { _, check ->
                viewModel.onTextAvailableAnswerCheck(check)
            }

            swAttachments.setOnCheckedChangeListener { _, check ->
                viewModel.onAttachmentsAvailableAnswerCheck(check)
            }

            cbCommentsEnable.setOnCheckedChangeListener { _, check ->
                viewModel.onCommentsEnableCheck(check)
            }

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



            viewModel.openAttachment.observe(viewLifecycleOwner, this@TaskEditorFragment::openFile)

            viewModel.availabilityDateRemoveVisibility.observe(viewLifecycleOwner) {
                ivRemoveAvailabilityDate.visibility = if (it) View.VISIBLE else View.GONE
            }

            Transformations.distinctUntilChanged(viewModel.filesVisibility)
                .observe(viewLifecycleOwner) {
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

            viewModel.showAttachments.observe(viewLifecycleOwner) {
                adapter.submit(it)
            }

            viewModel.openConfirmation.observe(viewLifecycleOwner) { (title, subtitle) ->
                ConfirmDialog.newInstance(title, subtitle)
                    .show(parentFragmentManager, null)
            }

            viewModel.availabilitySend.observe(viewLifecycleOwner) {
                if (cbAvailabilitySend.isChecked != it)
                    cbAvailabilitySend.isChecked = it
            }

            viewModel.commentsEnabled.observe(viewLifecycleOwner) {
                if (cbCommentsEnable.isChecked == it)
                    cbCommentsEnable.isChecked = it
            }

            lifecycleScope.launchWhenStarted {
                viewModel.answerType.collect {
                    swText.isChecked = it.textAvailable
                    tvCharsLimit.isEnabled = it.textAvailable
                    etCharsLimit.isEnabled = it.textAvailable
                    if (etCharsLimit.text.toString() != it.charsLimit)
                        etCharsLimit.setText(it.charsLimit)

                    swAttachments.isChecked = it.attachmentsAvailable
                    tvAttachmentsLimit.isEnabled = it.attachmentsAvailable
                    etAttachmentsLimit.isEnabled = it.attachmentsAvailable
                    if (etAttachmentsLimit.text.toString() != it.attachmentsLimit)
                        etAttachmentsLimit.setText(it.attachmentsLimit)

                    tvAttachmentsSizeLimit.isEnabled = it.attachmentsAvailable
                    etAttachmentsSizeLimit.isEnabled = it.attachmentsAvailable
                    tvAttachmentsLimitMb.isEnabled = it.attachmentsAvailable
                    if (etAttachmentsSizeLimit.text.toString() != it.attachmentsSizeLimit)
                        etAttachmentsSizeLimit.setText(it.attachmentsSizeLimit)
                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.markType.collect {
                    when (it) {
                        is TaskEditorViewModel.MarkTypeState.Score -> {
                            vfMarkType.displayedChild = 0
                            etMaxScore.setText(it.max)
                        }
                        is TaskEditorViewModel.MarkTypeState.Binary -> {
                            vfMarkType.displayedChild = 1
                        }
                    }
                    if (actMarkType.text.toString() != arrayAdapter.getItem(it.position)) {
                        actMarkType.setText(arrayAdapter.getItem(it.position) as String)
                    }
                }
            }

        }
    }


    private fun openFile(file: File) {
        // Get URI and MIME type of file
        // Open file with user selected app
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val apkURI = FileProvider.getUriForFile(
            context!!, context!!.applicationContext
                .packageName.toString() + ".provider", file
        )
        val extensionFromMimeType =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(file.toURI().toURL().toString())
        intent.setDataAndType(
            apkURI,
            extensionFromMimeType
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireActivity().startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Не получается открыть файл :(", Toast.LENGTH_SHORT)
                .show()
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
            ivOverlay.setImageDrawable(null)
            tvName.text = item.file.name
//            val uri = Uri.fromFile(item.file)
            val glide = Glide.with(ivFile)
            val load: RequestBuilder<Drawable> = when (item.file.getType()) {
                "image" -> {
                    glide.load(item.file)
                }
                "video" -> {
                    ivOverlay.setImageResource(R.drawable.play_video_btn)
                    glide.load(item.file)
                }
                "audio" -> {
                    glide.load(R.drawable.ic_audio_file)
                }
                "document" -> {
                    glide.load(R.drawable.ic_document_file)
                }
                "sheet" -> {
                    glide.load(R.drawable.ic_sheet_file)
                }
                "presentation" -> {
                    glide.load(R.drawable.ic_presentation_file)
                }
                "text" -> {
                    glide.load(R.drawable.ic_text_file)
                }
                "pdf" -> {
                    glide.load(R.drawable.ic_pdf_file)
                }
                else -> {
                    glide.load(R.drawable.ic_unknow_file)
                }
            }
            load.into(ivFile)
        }
    }


}