package com.denchic45.kts.ui.course.taskInfo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.SubmissionSettings
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentTaskInfoBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.ui.course.taskEditor.*
import com.denchic45.kts.utils.*
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding4.widget.textChanges
import kotlinx.coroutines.flow.collect
import java.io.File
import java.time.format.DateTimeFormatter

class TaskInfoFragment :
    BaseFragment<TaskInfoViewModel, FragmentTaskInfoBinding>(R.layout.fragment_task_info) {

    override val binding: FragmentTaskInfoBinding by viewBinding(FragmentTaskInfoBinding::bind)
    override val viewModel: TaskInfoViewModel by viewModels { viewModelFactory }
    private lateinit var appBarController: AppBarController
    private val btnActionSubmission: Button by lazy {
        View.inflate(requireContext(), R.layout.btn_action_submission, null) as Button
    }

    private val adapter =
        adapter {
            delegates(AttachmentAdapterDelegate(false))
            extensions {
                clickBuilder<AttachmentHolder> { onClick = { viewModel.onTaskFileClick(it) } }
            }
        }

    private val submissionAttachmentsAdapter =
        adapter {
            delegates(AttachmentAdapterDelegate(), AddAttachmentAdapterDelegate())
            extensions {
                clickBuilder<AttachmentHolder> {
                    onClick = {
                        viewModel.onSubmissionAttachmentClick(it)
                    }
                }
                clickBuilder<AttachmentHolder> {
                    view = { it.binding.ivFileRemove }
                    onClick = { position ->
                        viewModel.onRemoveSubmissionFileClick(position)
                    }
                }
                clickBuilder<AddAttachmentHolder> {
                    onClick = {
                        viewModel.onAddAttachmentClick()
                    }
                }
            }
        }

    private val fileChooser by lazy {
        FileViewer(requireActivity()) {
            Toast.makeText(
                requireContext(),
                "Невозможно открыть файл на данном устройстве",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var filePicker: FilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePicker = FilePicker(this, {
            with(it) {
                if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                    viewModel.onSelectedFile(File(requireContext().path(data!!.data!!)))
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarController = AppBarController.findController(requireActivity())

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPress()
                }
            })

        val bsBehavior = BottomSheetBehavior.from(binding.bsSubmission)

        binding.submissionExpanded.etText.textChanges()
            .compose(EditTextTransformer())
            .subscribe { viewModel.onSubmissionTextType(it) }

        binding.submissionExpanded.rvSubmissionAttachments.adapter = submissionAttachmentsAdapter

        bsBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                viewModel.onBottomSheetStateChanged(newState)
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    KeyboardUtils.hideKeyboardFrom(
                        requireContext(),
                        binding.submissionExpanded.etText
                    )
                    view.postDelayed({
                        appBarController.showToolbar()
                    }, 100)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                fun calcPercentOf(min: Float, max: Float, input: Float): Float {
                    return ((input - min) * 1) / (max - min)
                }

                val fl1 = calcPercentOf(0.2F, 0.8F, slideOffset)
                val fl2 = calcPercentOf(0.2F, 0.3F, slideOffset)
                binding.submissionExpanded.root.alpha = fl1
                binding.submissionCollapsed.root.alpha = 1F - fl2
            }
        })

        binding.rvAttachments.adapter = adapter

        viewModel.expandBottomSheet.observe(viewLifecycleOwner) { newState ->
            if (bsBehavior.state != newState) {
                bsBehavior.state = newState
            }
            binding.apply {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        submissionExpanded.root.translationZ = 1F
                        submissionCollapsed.root.translationZ = 0F

                        submissionExpanded.root.alpha = 1F
                        submissionCollapsed.root.alpha = 0F
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        submissionExpanded.root.translationZ = 0F
                        submissionCollapsed.root.translationZ = 1F

                        submissionExpanded.root.alpha = 0F
                        submissionCollapsed.root.alpha = 1F
                    }
                }
            }
        }



        viewModel.openConfirmation.observe(viewLifecycleOwner) { (title, subtitle) ->
            ConfirmDialog.newInstance(title, subtitle).show(childFragmentManager, null)
        }

        viewModel.finish.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.showMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        Transformations.distinctUntilChanged(viewModel.showSubmissionToolbar)
            .observe(viewLifecycleOwner) {
                if (it) {
                    appBarController.toolbar.addView(btnActionSubmission)
                    (btnActionSubmission.layoutParams as Toolbar.LayoutParams).apply {
                        gravity = Gravity.END
                        marginEnd = requireContext().dpToPx(16)
                    }
                    btnActionSubmission.apply {
                        alpha = 0F
                        animate().alpha(1F).setDuration(100).start()
                    }
                } else {
                    btnActionSubmission.animate()
                        .alpha(0F)
                        .setDuration(70)
                        .withEndAction { appBarController.toolbar.removeView(btnActionSubmission) }
                        .start()
                }
            }

        lifecycleScope.launchWhenStarted {
            viewModel.taskViewState.collect { it ->
                with(binding) {
                    tvTaskName.text = it.name
                    tvDescription.text = it.description
                    tvDescription.visibility =
                        if (it.description.isEmpty()) View.GONE else View.VISIBLE
                    it.dateWithTimeLeft?.let {
                        chpDate.visibility = View.VISIBLE
                        tvTimeLeft.visibility = View.VISIBLE
                        chpDate.text = it.first
                        tvTimeLeft.text = it.second
                    } ?: run {
                        chpDate.visibility = View.GONE
                        tvTimeLeft.visibility = View.GONE
                    }
                    tvComments.text = "Написать комментарий"
                }
                setSubmissionContentVisibility(it.submissionSettings)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.taskAttachments.collect {
                binding.tvAttachmentsHeader.visibility =
                    if (it.isEmpty()) View.GONE else View.VISIBLE
                adapter.submit(it)
            }
        }

        binding.apply {

            btnActionSubmission.setOnClickListener { viewModel.onActionClick() }
            submissionCollapsed.btnAction.setOnClickListener { viewModel.onActionClick() }

            lifecycleScope.launchWhenCreated {
                viewModel.submissionViewState.collect { submission ->

                    fun setTeacherData(teacher: User) {
                        Glide.with(requireContext())
                            .load(teacher.photoUrl)
                            .into(submissionExpanded.ivAvatar)
                        submissionExpanded.tvTeacherName.text = teacher.fullName
                        submissionExpanded.grpTeacher.visibility = View.VISIBLE
                    }

                    when (submission.status) {
                        is Task.SubmissionStatus.NotSubmitted -> {
                            submissionCollapsed.tvSubmissionState.text = "Не сдано"

                            submissionCollapsed.tvSubmissionDescription.text = ""
                            submissionCollapsed.tvSubmissionDescription.visibility = View.GONE


                            applyActionButtonProperties {
                                setTextColor(Color.WHITE)
                                text = if (submission.content.isEmpty()) "Добавить" else "Отправить"
                                setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), R.color.blue)
                                )
                            }

                            submissionExpanded.tvSubmissionState.text = "Не сдано"

                            submissionExpanded.tvSubmissionDescription.text = ""
                            submissionExpanded.tvSubmissionDescription.visibility = View.GONE

                            submissionExpanded.grpTeacher.visibility = View.GONE
                            setSubmissionContent(submission.content, true)
                        }
                        is Task.SubmissionStatus.Submitted -> {
                            submissionCollapsed.tvSubmissionState.text = "Сдано на проверку"
                            submissionExpanded.tvSubmissionState.text = "Сдано на проверку"

                            val description = DateTimeFormatter.ofPattern("dd MMM HH:mm")
                                .format(submission.status.submittedDate)

                            submissionCollapsed.tvSubmissionDescription.text = description
                            submissionCollapsed.tvSubmissionDescription.visibility = View.VISIBLE
                            submissionExpanded.tvSubmissionDescription.text = description
                            submissionExpanded.tvSubmissionDescription.visibility = View.VISIBLE


                            applyActionButtonProperties {
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                                text = "Отменить"
                                setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.alpha_red_10
                                    )
                                )
                            }
                            submissionExpanded.grpTeacher.visibility = View.GONE
                            setSubmissionContent(submission.content, false)
                        }
                        is Task.SubmissionStatus.Graded -> {

                            setTeacherData(submission.status.teacher)
                            setSubmissionContent(submission.content, false)
                        }
                        is Task.SubmissionStatus.Rejected -> {

                            setTeacherData(submission.status.teacher)
                            setSubmissionContent(submission.content, true)
                        }
                    }

                    measureBottomSheetPeek(bsBehavior)
                }
            }
            viewModel.focusOnTextField.observe(viewLifecycleOwner) {
                submissionExpanded.etText.requestFocus()
            }
        }

        viewModel.openAttachment.observe(viewLifecycleOwner) { fileChooser.openFile(it) }

        viewModel.openFilePicker.observe(viewLifecycleOwner) { filePicker.selectFiles() }
    }



    private fun setSubmissionContent(content: Task.Submission.Content, allowEditContent: Boolean) {
        submissionAttachmentsAdapter.submit(
            if (allowEditContent)
                content.attachments + AddAttachmentItem
            else
                content.attachments
        )

        with(binding.submissionExpanded) {
            if (!etText.text.contentEquals(content.text)) {
                etText.setText(content.text)
            }
            etText.isEnabled = allowEditContent
        }
    }

    private fun setSubmissionContentVisibility(submissionSettings: SubmissionSettings) {
        with(binding.submissionExpanded) {
            val textVisibility =
                if (submissionSettings.textAvailable) View.VISIBLE
                else View.GONE

            tvTextHeader.visibility = textVisibility
            etText.filters = arrayOf(InputFilter.LengthFilter(submissionSettings.charsLimit))
            tilText.visibility = textVisibility


            val attachmentsVisibility =
                if (submissionSettings.attachmentsAvailable) View.VISIBLE
                else View.GONE

            Toast.makeText(
                requireContext(),
                submissionSettings.attachmentsAvailable.toString(),
                Toast.LENGTH_SHORT
            ).show()

            tvAttachmentsHeader.visibility = attachmentsVisibility
            rvSubmissionAttachments.visibility = attachmentsVisibility
        }
    }

    private fun measureBottomSheetPeek(bsBehavior: BottomSheetBehavior<FrameLayout>) {
        binding.submissionCollapsed.root.post {
            bsBehavior.setPeekHeight(
                binding.submissionCollapsed.clRoot.height + Dimensions.dpToPx(
                    24, requireContext()
                )
            )
        }
    }

    private fun applyActionButtonProperties(buttonReceiver: Button.() -> Unit) {
        buttonReceiver(btnActionSubmission)
        buttonReceiver(binding.submissionCollapsed.btnAction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (appBarController.toolbar.contains(btnActionSubmission))
            appBarController.toolbar.removeView(btnActionSubmission)
    }

    companion object {
        const val TASK_ID = "TaskInfo TASK_ID"
        const val COURSE_ID = "TaskInfo COURSE_ID"
    }
}