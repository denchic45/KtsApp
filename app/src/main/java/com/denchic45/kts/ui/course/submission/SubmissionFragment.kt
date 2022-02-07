package com.denchic45.kts.ui.course.submission

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentSubmissionBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.example.utils.DimensionUtils.dpToPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SubmissionFragment : BottomSheetDialogFragment() {

    private val binding: FragmentSubmissionBinding by viewBinding(FragmentSubmissionBinding::bind)
    lateinit var viewModelFactory: ViewModelFactory<SubmissionViewModel>
    private val viewModel: SubmissionViewModel by viewModels { viewModelFactory }

    private lateinit var callback: BottomSheetBehavior.BottomSheetCallback
    private lateinit var behavior: BottomSheetBehavior<*>

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
                    ((bottomSheet.parent as View).height - bottomSheet.top - binding.footer.height).toFloat()
                if (slideOffset < -0.1)
                    return
                binding.footer.y = bottomY
            }
        }

        behavior.addBottomSheetCallback(callback.apply {
            view.postDelayed({ onSlide(binding.root.parent as View, 0f) }, 100)
        })

        with(binding) {

        }
    }
}