package com.denchic45.kts.ui.course

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.databinding.FragmentTaskBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.course.taskEditor.AttachmentAdapterDelegate
import com.denchic45.kts.utils.Dimensions
import com.denchic45.widget.extendedAdapter.adapter
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collect

class TaskFragment : BaseFragment<TaskViewModel, FragmentTaskBinding>(R.layout.fragment_task) {

    override val binding: FragmentTaskBinding by viewBinding(FragmentTaskBinding::bind)
    override val viewModel: TaskViewModel by viewModels { viewModelFactory }
     val appBarController = AppBarController.findController(requireActivity())

    private val adapter = adapter {
        delegates(AttachmentAdapterDelegate())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bsBehavior = BottomSheetBehavior.from(binding.bsSubmission)

        bsBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                viewModel.onBottomSheetStateChanged(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                fun calc(min: Float, max: Float, input: Float): Float {
                    return ((input - min) * 1) / (max - min)
                }

                val fl1 = calc(0.4F, 0.8F, slideOffset)
                val fl2 = calc(0.2F, 0.5F, slideOffset)
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

        viewModel.showSubmissionToolbar.observe(viewLifecycleOwner) {
            if (it) {
                appBarController.toolbar.visibility = View.GONE
                appBarController.addView() //todo Добавить view тулбара с кнопкой справа
            } else {
                appBarController.toolbar.visibility = View.VISIBLE
                appBarController.removeView()
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
            }
        }

        binding.apply {
            lifecycleScope.launchWhenStarted {
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
                            submissionCollapsed.btnAction.text = "Добавить"
                            submissionCollapsed.btnAction.setBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.blue)
                            )
                            submissionCollapsed.btnAction.setTextColor(Color.WHITE)

                            submissionExpanded.tvSubmissionState.text = "Не сдано"
                            submissionExpanded.grpTeacher.visibility = View.GONE
                        }
                        is Task.Submission.Draft -> {
                            submissionCollapsed.tvSubmissionState.text = "Черновик"
                            submissionCollapsed.btnAction.text = "Отправить"
                            submissionCollapsed.btnAction.setBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.yellow)
                            )
                            ContextCompat.getColor(requireContext(), R.color.dark)

                            submissionExpanded.tvSubmissionState.text = "Черновик"
                            submissionExpanded.grpTeacher.visibility = View.GONE
                        }
                        is Task.Submission.Done -> {

                            submissionExpanded.grpTeacher.visibility = View.GONE
                        }
                        is Task.Submission.Graded -> {

                            setTeacherData(submission.teacher)
                        }
                        is Task.Submission.Rejected -> {

                            setTeacherData(submission.teacher)
                        }
                    }

                    binding.submissionCollapsed.root.post {
                        bsBehavior.setPeekHeight(
                            binding.submissionCollapsed.clRoot.height + Dimensions.dpToPx(
                                24,
                                requireContext()
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TASK_ID = "TASK_ID"
    }
}