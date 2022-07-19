package com.denchic45.kts.ui.group.courses

import android.os.Bundle
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.domain.model.CourseHeader
import com.denchic45.kts.databinding.FragmentGroupCoursesBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.adapter.CourseAdapter
import com.denchic45.kts.util.collectWhenStarted

class GroupCoursesFragment : BaseFragment<GroupCoursesViewModel, FragmentGroupCoursesBinding>(
    R.layout.fragment_group_courses
) {
    override val binding by viewBinding(FragmentGroupCoursesBinding::bind)

    override val viewModel: GroupCoursesViewModel by viewModels { viewModelFactory }
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CourseAdapter(
            viewModel::onCourseItemClick,
            viewModel::onCourseLongItemClick
        )
        with(binding) {
            rvCourse.adapter = adapter
        }

        viewModel.courses.collectWhenStarted(lifecycleScope) { courses: List<CourseHeader> ->
            adapter.submitList(courses)
        }

        viewModel.selectItem.observe(viewLifecycleOwner) { (position, select) ->
            selectSubjectTeacherItem(position, select)
        }

        viewModel.clearItemsSelection.observe(viewLifecycleOwner) { positions: Set<Int> ->
            positions.forEach { position: Int -> selectSubjectTeacherItem(position, false) }
        }
    }

    private fun selectSubjectTeacherItem(position: Int, select: Boolean) {
        val holder =
            binding.rvCourse.findViewHolderForLayoutPosition(position) as CourseAdapter.CourseHolder?
        holder!!.setSelect(select)
    }

    private fun finishActionMode() {
        if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        finishActionMode()
        binding.rvCourse.adapter = null
    }


    companion object {

        const val GROUP_ID = "GroupCourse GROUP_ID"

        fun newInstance(groupId: String?): GroupCoursesFragment {
            val fragment = GroupCoursesFragment()
            val args = Bundle()
            args.putString(GROUP_ID, groupId)
            fragment.arguments = args
            return fragment
        }
    }

}