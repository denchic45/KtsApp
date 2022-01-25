package com.denchic45.kts.ui.course

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.SubmissionSettings
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentTaskBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.course.taskEditor.AddAttachmentAdapterDelegate
import com.denchic45.kts.ui.course.taskEditor.AddAttachmentItem
import com.denchic45.kts.ui.course.taskEditor.AttachmentAdapterDelegate
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.dpToPx
import com.denchic45.widget.extendedAdapter.adapter
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collect

class TaskFragment : BaseFragment<TaskViewModel, FragmentTaskBinding>(R.layout.fragment_task) {

    override val binding: FragmentTaskBinding by viewBinding(FragmentTaskBinding::bind)
    override val viewModel: TaskViewModel by viewModels { viewModelFactory }
    private val appBarController by lazy {
        AppBarController.findController(requireActivity())
    }
    private val btnActionSubmission: Button by lazy {
        View.inflate(requireContext(), R.layout.btn_action_submission, null) as Button
    }

    private val adapter =
        adapter {
            delegates(AttachmentAdapterDelegate())
        }

    private val submissionAttachmentsAdapter =
        adapter {
            delegates(AttachmentAdapterDelegate(), AddAttachmentAdapterDelegate())
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bsBehavior = BottomSheetBehavior.from(binding.bsSubmission)

        appBarController.toolbar.title = ""

        binding.submissionExpanded.rvSubmissionAttachments.adapter = submissionAttachmentsAdapter

        bsBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                viewModel.onBottomSheetStateChanged(newState)
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
            viewModel.taskViewState.collect {
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
                    tvAttachmentsHeader.visibility =
                        if (it.attachments.isEmpty()) View.GONE else View.VISIBLE
                    adapter.submit(it.attachments)
                    tvComments.text = "Написать комментарий"
                }
                setSubmissionContentVisibility(it.submissionSettings)
            }
        }

        binding.apply {
            lifecycleScope.launchWhenCreated {
                viewModel.submissionViewState.collect { submission ->

                    fun setTeacherData(teacher: User) {
                        Glide.with(requireContext())
                            .load(teacher.photoUrl)
                            .into(submissionExpanded.ivAvatar)
                        submissionExpanded.tvTeacherName.text = teacher.fullName
                        submissionExpanded.grpTeacher.visibility = View.VISIBLE
                    }

                    when (submission) {
                        is Task.Submission.Nothing -> {
                            submissionCollapsed.tvSubmissionState.text = "Не сдано"

                            submissionCollapsed.tvSubmissionDescription.text = ""
                            submissionCollapsed.tvSubmissionDescription.visibility = View.GONE


                            applyActionButtonProperties {
                                setTextColor(Color.WHITE)
                                text = "Добавить"
                                setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), R.color.blue)
                                )
                            }

                            submissionExpanded.tvSubmissionState.text = "Не сдано"

                            submissionExpanded.tvSubmissionDescription.text = ""
                            submissionExpanded.tvSubmissionDescription.visibility = View.GONE

                            submissionExpanded.grpTeacher.visibility = View.GONE
                            submissionAttachmentsAdapter.submit(listOf(AddAttachmentItem))
                        }
                        is Task.Submission.Draft -> {
                            submissionCollapsed.tvSubmissionState.text = "Черновик"
                            applyActionButtonProperties {
                                text = "Отправить"
                                setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), R.color.yellow)
                                )
                            }
                            ContextCompat.getColor(requireContext(), R.color.dark)

                            submissionExpanded.tvSubmissionState.text = "Черновик"
                            submissionExpanded.grpTeacher.visibility = View.GONE

                            setSubmissionContent(submission.content)
                        }
                        is Task.Submission.Done -> {

                            submissionExpanded.grpTeacher.visibility = View.GONE
                            setSubmissionContent(submission.content)
                        }
                        is Task.Submission.Graded -> {

                            setTeacherData(submission.teacher)
                            setSubmissionContent(submission.content)
                        }
                        is Task.Submission.Rejected -> {

                            setTeacherData(submission.teacher)
                            setSubmissionContent(submission.content)
                        }
                    }

                    measureBottomSheetPeek(bsBehavior)
                }
            }
        }
    }

    private fun setSubmissionContent(content: Task.Submission.Content) {
        submissionAttachmentsAdapter.submit(content.attachments + AddAttachmentItem)
        binding.submissionExpanded.etText.setText(content.text)
    }

    private fun setSubmissionContentVisibility(submissionSettings: SubmissionSettings) {
        with(binding.submissionExpanded) {
            val textVisibility =
                if (submissionSettings.textAvailable) View.VISIBLE
                else View.GONE

            tvTextHeader.visibility = textVisibility
            tilText.visibility = textVisibility

            val attachmentsVisibility =
                if (submissionSettings.attachmentsAvailable) View.VISIBLE
                else View.GONE

            tvAttachmentsHeader.visibility = attachmentsVisibility
            rvSubmissionAttachments.visibility = attachmentsVisibility
        }
    }

    private fun measureBottomSheetPeek(bsBehavior: BottomSheetBehavior<FrameLayout>) {
        binding.submissionCollapsed.root.post {
            bsBehavior.setPeekHeight(
                binding.submissionCollapsed.clRoot.height + Dimensions.dpToPx(
                    24,
                    requireContext()
                )
            )
        }
    }

    private fun applyActionButtonProperties(buttonReceiver: Button.() -> Unit) {
        buttonReceiver(btnActionSubmission)
        buttonReceiver(binding.submissionCollapsed.btnAction)
    }

    companion object {
        const val TASK_ID = "TASK_ID"
    }
}