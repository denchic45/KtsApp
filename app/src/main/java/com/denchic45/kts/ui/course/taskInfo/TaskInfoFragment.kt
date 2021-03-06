package com.denchic45.kts.ui.course.taskInfo

import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.contains
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.R
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.domain.model.SubmissionSettings
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.databinding.FragmentTaskInfoBinding
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.course.taskEditor.AddAttachmentAdapterDelegate
import com.denchic45.kts.ui.course.taskEditor.AddAttachmentHolder
import com.denchic45.kts.ui.course.taskEditor.AttachmentAdapterDelegate
import com.denchic45.kts.ui.course.taskEditor.AttachmentHolder
import com.denchic45.kts.util.*
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding4.widget.textChanges

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
                "???????????????????? ?????????????? ???????? ???? ???????????? ????????????????????",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var filePicker: FilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filePicker = FilePicker(this, {
            it?.let { viewModel.onSelectedFile(it[0]) }
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
                    binding.submissionExpanded.etText.closeKeyboard()
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
                        tvTimeLeft.text = "???????????????? " + resources.getQuantityString(
                            R.plurals.day,
                            it.second.quantity,
                            it.second.quantity
                        )
                    } ?: run {
                        chpDate.visibility = View.GONE
                        tvTimeLeft.visibility = View.GONE
                    }
                    tvComments.text = "???????????????? ??????????????????????"
                }
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

            fun setTeacherData(teacher: User?) {
                teacher?.let {
                    Glide.with(requireContext())
                        .load(teacher.photoUrl)
                        .into(submissionExpanded.ivAvatar)
                    submissionExpanded.tvTeacherName.text = teacher.fullName
                    submissionExpanded.grpTeacher.visibility = View.VISIBLE
                } ?: run {
                    submissionExpanded.grpTeacher.visibility = View.GONE
                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.submissionViewState2.collect { viewState ->

                    applyActionButtonProperties {
                        if (viewState.btnVisibility) {
                            visibility = View.VISIBLE
                            setTextColor(requireContext().colors(viewState.btnTextColor))
                            setBackgroundColor(requireContext().colors(viewState.btnBackgroundColor))
                            text = viewState.btnText

                        } else {
                            visibility = View.GONE
                        }
                    }

                    submissionExpanded.tvExpandState.text = viewState.title
                    submissionCollapsed.tvCollapseState.text = viewState.title

                    if (viewState.subtitleVisibility) {
                        submissionCollapsed.tvCollapseDescription.visibility = View.VISIBLE
                        submissionExpanded.tvExpandDescription.visibility = View.VISIBLE
                        submissionCollapsed.tvCollapseDescription.text = viewState.subtitle
                        submissionExpanded.tvExpandDescription.text = viewState.subtitle
                    } else {
                        submissionCollapsed.tvCollapseDescription.visibility = View.GONE
                        submissionExpanded.tvExpandDescription.visibility = View.GONE
                    }

                    setTeacherData(viewState.teacher)
                    setSubmissionContent(
                        viewState.textContent,
                        viewState.attachments,
                        viewState.allowEditContent
                    )
                    setSubmissionContentVisibility(
                        viewState.submissionSettings,
                        viewState.attachments.isNotEmpty()
                    )

                    measureBottomSheetPeek(bsBehavior)
                }
            }


            viewModel.focusOnTextField.observe(viewLifecycleOwner) {
                submissionExpanded.etText.showKeyboard()
            }
        }

        viewModel.openAttachment.observe(viewLifecycleOwner) { fileChooser.openFile(it) }

        viewModel.openFilePicker.observe(viewLifecycleOwner) { filePicker.selectFiles() }
    }


    private fun setSubmissionContent(
        textContent: String,
        attachments: List<DomainModel>,
        allowEditContent: Boolean
    ) {
        submissionAttachmentsAdapter.submit(attachments)

        with(binding.submissionExpanded) {
            if (!etText.text.contentEquals(textContent)) {
                etText.setText(textContent)
            }
            etText.isEnabled = allowEditContent
        }
    }

    private fun setSubmissionContentVisibility(
        submissionSettings: SubmissionSettings,
        isAttachmentsNotEmpty: Boolean
    ) {
        with(binding.submissionExpanded) {
            val textVisibility =
                if (submissionSettings.textAvailable) View.VISIBLE
                else View.GONE

            tvTextHeader.visibility = textVisibility
            etText.filters = arrayOf(InputFilter.LengthFilter(submissionSettings.charsLimit))
            tilText.visibility = textVisibility

            val attachmentsVisibility =
                if (submissionSettings.attachmentsAvailable && isAttachmentsNotEmpty) View.VISIBLE
                else View.GONE

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
