package com.denchic45.kts.ui.course.taskEditor

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
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
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.databinding.FragmentTaskEditorBinding
import com.denchic45.kts.databinding.ItemAddAttachmentBinding
import com.denchic45.kts.databinding.ItemAttachmentBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.kts.ui.course.sectionPicker.SectionPickerFragment
import com.denchic45.kts.ui.course.sectionPicker.SectionPickerViewModel
import com.denchic45.kts.utils.FilePicker
import com.denchic45.kts.utils.ValueFilter
import com.denchic45.kts.utils.getType
import com.denchic45.kts.utils.viewBinding
import com.denchic45.widget.extendedAdapter.ListItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import com.jakewharton.rxbinding4.widget.textChanges
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject
import kotlin.properties.Delegates


class TaskEditorFragment :
    BaseFragment<TaskEditorViewModel, FragmentTaskEditorBinding>(
        R.layout.fragment_task_editor,
        R.menu.options_task_editor
    ) {

    companion object {
        const val TASK_ID = "TaskEditor TASK_ID"
        const val COURSE_ID = "TaskEditor COURSE_ID"
        const val SECTION_ID = "SECTION_ID"
    }

    private var oldToolbarScrollFlags by Delegates.notNull<Int>()

    private lateinit var appBarController: AppBarController

    private val adapter = adapter {
        delegates(AttachmentAdapterDelegate())
        extensions {
            clickBuilder<AttachmentHolder> {
                view = { it.binding.ivFileRemove }
                onClick = { position ->
                    viewModel.onRemoveFileClick(position)
                }
            }
            clickBuilder<AttachmentHolder> {
                onClick = { position: Int -> viewModel.onAttachmentClick(position) }
            }
        }
    }

    private lateinit var filePicker: FilePicker

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var sectionPickerViewModelFactory: ViewModelFactory<SectionPickerViewModel>

    private val sectionPickerViewModel: SectionPickerViewModel by viewModels(
        ownerProducer = { this }, factoryProducer = { sectionPickerViewModelFactory }
    )

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePicker = FilePicker(this, {
            it?.let { viewModel.onAttachmentsSelect(it) }
        }, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarController = AppBarController.findController(requireActivity()).apply {
            oldToolbarScrollFlags = toolbarScrollFlags
            setExpanded(true, false)
            toolbarScrollFlags = 0
        }
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

            etCharsLimit.filters = arrayOf(ValueFilter(0, 2000), InputFilter.LengthFilter(4))

            etAttachmentsLimit.filters = arrayOf(ValueFilter(0, 99), InputFilter.LengthFilter(2))

            etAttachmentsSizeLimit.filters =
                arrayOf(ValueFilter(0, 999), InputFilter.LengthFilter(3))

            actSection.setOnTouchListener { _, event ->
                if (MotionEvent.ACTION_DOWN == event.action)
                    viewModel.onSectionClick()
                false
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

            viewModel.showErrorMessage.observe(viewLifecycleOwner) { idWithMessagePair ->
                val fieldView = view.findViewById<View>(
                    idWithMessagePair.first
                )

                if (fieldView is TextInputLayout) {
                    if (idWithMessagePair.second != null) {
                        fieldView.error = idWithMessagePair.second
                    } else {
                        fieldView.error = null
                    }
                } else if (fieldView is EditText) {
                    if (idWithMessagePair.second != null) {
                        fieldView.error = idWithMessagePair.second
                    } else {
                        fieldView.error = null
                    }
                }
            }

            viewModel.nameField.observe(viewLifecycleOwner) {
                if (etName.text.toString() != it) etName.setText(it)
            }

            viewModel.descriptionField.observe(viewLifecycleOwner) {
                if (etDescription.text.toString() != it) etDescription.setText(it)
            }

            viewModel.showCompletionDate.observe(viewLifecycleOwner) {
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

            viewModel.sectionField.observe(viewLifecycleOwner, actSection::setText)

            viewModel.openCourseSections.observe(viewLifecycleOwner) { (courseId, selectedSection) ->
                sectionPickerViewModel.currentSelectedSection = selectedSection
                SectionPickerFragment.newInstance(courseId)
                    .show(childFragmentManager, null)
            }

            lifecycleScope.launchWhenStarted {
                sectionPickerViewModel.selectedSectionId.collect {
                    viewModel.onSectionSelected(it)
                }
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

            viewModel.disabledSendAfterDate.observe(viewLifecycleOwner) {
                if (cbAvailabilitySend.isChecked != it)
                    cbAvailabilitySend.isChecked = it
            }

            viewModel.commentsEnabled.observe(viewLifecycleOwner) {
                if (cbCommentsEnable.isChecked != it)
                    cbCommentsEnable.isChecked = it
            }

            lifecycleScope.launchWhenStarted {
                viewModel.submissionSettings.collect {
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
        }
    }

    private fun openFile(file: File) {
        // Get URI and MIME type of file
        // Open file with user selected app
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            val apkURI = FileProvider.getUriForFile(
                requireContext(),
                requireActivity().applicationContext
                    .packageName.toString() + ".provider", file
            )
            val extensionFromMimeType = MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(file.toURI().toURL().toString())
            setDataAndType(apkURI, extensionFromMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            requireActivity().startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Не получается открыть файл :(", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBarController.toolbarScrollFlags = oldToolbarScrollFlags
    }
}

class AttachmentAdapterDelegate(private val crossBtnVisibility: Boolean = true) :
    ListItemAdapterDelegate<Attachment, AttachmentHolder>() {
    override fun isForViewType(item: Any): Boolean = item is Attachment

    override fun onBindViewHolder(item: Attachment, holder: AttachmentHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): AttachmentHolder {
        return AttachmentHolder(
            parent.viewBinding(ItemAttachmentBinding::inflate),
            crossBtnVisibility
        )
    }
}

class AddAttachmentAdapterDelegate :
    ListItemAdapterDelegate<AddAttachmentItem, AddAttachmentHolder>() {
    override fun isForViewType(item: Any): Boolean = item is AddAttachmentItem

    override fun onBindViewHolder(item: AddAttachmentItem, holder: AddAttachmentHolder) {
        holder.onBind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): AddAttachmentHolder {
        return AddAttachmentHolder(parent.viewBinding(ItemAddAttachmentBinding::inflate))
    }

}

object AddAttachmentItem : DomainModel() {
    override fun equals(other: Any?): Boolean = other is AddAttachmentItem
}

class AddAttachmentHolder(itemAddAttachmentBinding: ItemAddAttachmentBinding) :
    BaseViewHolder<AddAttachmentItem, ItemAddAttachmentBinding>(itemAddAttachmentBinding) {
    override fun onBind(item: AddAttachmentItem) {

    }
}

class AttachmentHolder(
    itemAttachmentBinding: ItemAttachmentBinding,
    private val crossBtnVisibility: Boolean
) :
    BaseViewHolder<Attachment, ItemAttachmentBinding>(itemAttachmentBinding) {
    override fun onBind(item: Attachment) {
        with(binding) {
            if (!crossBtnVisibility) {
                ivFileRemove.visibility = View.GONE
            }
            ivOverlay.setImageDrawable(null)
            tvName.text = item.name
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
            load
                .placeholder(R.drawable.loading_attachment)
                .into(ivFile)
        }
    }


}