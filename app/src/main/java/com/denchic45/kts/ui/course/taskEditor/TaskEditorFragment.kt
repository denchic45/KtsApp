package com.denchic45.kts.ui.course.taskEditor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.databinding.FragmentTaskEditorBinding
import com.denchic45.kts.ui.BaseFragment

class TaskEditorFragment : BaseFragment<TaskEditorViewModel, FragmentTaskEditorBinding>() {

    override val viewModel: TaskEditorViewModel by viewModels { viewModelFactory }

    override val binding: FragmentTaskEditorBinding by viewBinding(FragmentTaskEditorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}