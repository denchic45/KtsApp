package com.denchic45.kts.ui.course.submission

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.databinding.FragmentSubmissionBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.rx.EditTextTransformer
import com.denchic45.kts.ui.course.taskEditor.AttachmentAdapterDelegate
import com.denchic45.kts.utils.ValueFilter
import com.denchic45.kts.utils.closeKeyboard
import com.denchic45.widget.extendedAdapter.ItemAdapterDelegate
import com.denchic45.widget.extendedAdapter.adapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding4.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class SubmissionDialog : BottomSheetDialogFragment() {

    private val binding: FragmentSubmissionBinding by viewBinding(FragmentSubmissionBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<SubmissionViewModel>
    private val viewModel: SubmissionViewModel by viewModels { viewModelFactory }

    private lateinit var callback: BottomSheetBehavior.BottomSheetCallback
    private lateinit var behavior: BottomSheetBehavior<*>

    companion object {
        const val TASK_ID = "Submission TASK_ID"
        const val STUDENT_ID = "Submission STUDENT_ID"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_submission, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        behavior = (dialog as BottomSheetDialog).behavior
        callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val bottomY =
                    ((bottomSheet.parent as View).measuredHeight - bottomSheet.top - binding.footer.measuredHeight).toFloat()

                if (slideOffset < -0)
                    return
                binding.footer.y = bottomY
            }
        }

        behavior.addBottomSheetCallback(callback)

        binding.clRoot.viewTreeObserver.addOnGlobalLayoutListener {

            behavior.peekHeight = if (binding.vf.displayedChild == 0) {
                binding.flMain.height
            } else {
                binding.clReject.height
            }

            callback.onSlide(binding.root.parent as View, 0f)
        }

        with(binding) {

            etGrade.filters = arrayOf(ValueFilter(1, 5))

            etGrade.textChanges()
                .skip(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .subscribe(viewModel::onGradeType)

            etCause.textChanges()
                .skip(1)
                .filter(CharSequence::isNotEmpty)
                .compose(EditTextTransformer())
                .subscribe(viewModel::onCauseType)

            ivReject.setOnClickListener {
                viewModel.onRejectClick()
            }

            tilGrade.setEndIconOnClickListener { viewModel.onSendGradeClick() }

            btnCommentSend.setOnClickListener {
                if (tilCommentSend.visibility == View.GONE) {
                    tilCommentSend.visibility = View.VISIBLE
                    val keyboard: InputMethodManager =
                        requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    etCommentSend.requestFocus()
                    keyboard.showSoftInput(etCommentSend, 0)
                } else {
                    tilCommentSend.visibility = View.GONE
                    etCommentSend.closeKeyboard()
                }
            }


            val attachmentAdapter = adapter { delegates(AttachmentAdapterDelegate(false)) }
            val commentAdapter = adapter { delegates(ItemAdapterDelegate()) }

            rvAttachments.adapter = attachmentAdapter
            rvComments.adapter = commentAdapter

            lifecycleScope.launchWhenStarted {
                viewModel.gradeButtonVisibility.collect {
                    tilGrade.isEndIconVisible = it
                }
            }

            lifecycleScope.launchWhenStarted {
                viewModel.showSubmission.collect {

                    Glide.with(requireContext())
                        .load(it.student.photoUrl)
                        .into(ivAvatar)

                    tvName.text = it.student.fullName

                    if (it.content.hasText()) {
                        tvAnswerText.visibility = View.VISIBLE
                        tvAnswerText.text = it.content.text
                    } else {
                        tvAnswerText.visibility = View.GONE
                    }

                    if (it.content.hasAttachments()) {
                        rvAttachments.visibility = View.VISIBLE
                        attachmentAdapter.submit(it.content.attachments)
                    } else {
                        rvAttachments.visibility = View.GONE
                    }

                    tvStatus.text = when (it.status) {
                        Task.SubmissionStatus.NotSubmitted -> {
                            "Не сдано"
                        }
                        is Task.SubmissionStatus.Submitted -> {
                            "Сдано: ${
                                DateTimeFormatter.ofPattern("dd MMM HH:mm")
                                    .format(it.status.submittedDate)
                            }"
                        }
                        is Task.SubmissionStatus.Graded -> {
                            etGrade.setText(it.status.grade.toString())
                            "Оценено: ${it.status.grade}/5"
                        }
                        is Task.SubmissionStatus.Rejected -> {
                            "Отклонено по причине: ${it.status.cause}"
                        }
                    }
                }
            }

            viewModel.openRejectConfirmation.observe(viewLifecycleOwner) {
                vf.displayedChild = 1
                animateHeight(requireActivity().windowHeight())
            }

            viewModel.closeRejectConfirmation.observe(viewLifecycleOwner) {
                vf.displayedChild = 0
                animateHeight(requireActivity().windowHeight())
            }

            btnCancel.setOnClickListener { viewModel.onRejectCancelClick() }

            btnReject.setOnClickListener { viewModel.onRejectConfirmClick() }
        }
    }

    private fun animateHeight(windowHeight:Int) {

         fun preMeasureViewHeight(): Int {
            val currentView = binding.vf.currentView
            currentView.measure(windowWidth, windowHeight)
            return currentView.measuredHeight
        }

        fun changeBottomSheetHeight(`val`: Int) {
            val layoutParams: ViewGroup.LayoutParams = binding.root.layoutParams
            layoutParams.height = `val`
            binding.root.layoutParams = layoutParams
        }

        val newHeight = preMeasureViewHeight() + binding.vf.paddingBottom
        val anim = ValueAnimator.ofInt(
            requireView().height,
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) (windowHeight).coerceAtMost(
                newHeight
            )
            else
                (windowHeight - windowWidth * 9 / 16).coerceAtMost(newHeight)
        )

        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            changeBottomSheetHeight(`val`)
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 300
        anim.start()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                changeBottomSheetHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        })
    }




    private inline val windowWidth: Int
        get() {
            val view = requireActivity().window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            return resources.displayMetrics.widthPixels - insets.left - insets.right
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
}

fun Activity.windowHeight(): Int {
    val view = window.decorView
    return resources.displayMetrics.heightPixels
}