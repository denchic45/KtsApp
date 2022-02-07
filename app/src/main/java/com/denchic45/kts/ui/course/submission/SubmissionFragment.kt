package com.denchic45.kts.ui.course.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentSubmissionBinding
import com.denchic45.kts.ui.BaseFragment

class SubmissionFragment :
    BaseFragment<SubmissionViewModel, FragmentSubmissionBinding>(R.layout.fragment_submission) {

    override val binding: FragmentSubmissionBinding by viewBinding(FragmentSubmissionBinding::bind)
    override val viewModel: SubmissionViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

        }
    }
}