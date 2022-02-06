package com.denchic45.kts.ui.course.submissions

import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentSubmissionsBinding
import com.denchic45.kts.ui.BaseFragment

class SubmissionsFragment :
    BaseFragment<SubmissionsViewModel, FragmentSubmissionsBinding>(R.layout.fragment_submissions) {
    override val viewModel: SubmissionsViewModel by viewModels { viewModelFactory }
    override val binding: FragmentSubmissionsBinding by viewBinding(FragmentSubmissionsBinding::bind)

}