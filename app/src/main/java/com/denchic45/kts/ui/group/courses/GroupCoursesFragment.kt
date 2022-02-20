package com.denchic45.kts.ui.group.courses

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.databinding.FragmentGroupCoursesBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.CourseAdapter
import com.denchic45.kts.ui.confirm.ConfirmDialog
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class GroupCoursesFragment : Fragment(R.layout.fragment_group_courses) {
    private val compositeDisposable = CompositeDisposable()
    private val viewBinding by viewBinding(FragmentGroupCoursesBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GroupCoursesViewModel>
    private val viewModel: GroupCoursesViewModel by viewModels { viewModelFactory }
    private var adapter: CourseAdapter? = null
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            adapter = CourseAdapter({ position: Int -> viewModel.onCourseItemClick(position) },
                { position: Int -> viewModel.onCourseLongItemClick(position) })
            rvCourse.adapter = adapter
        }

        lifecycleScope.launchWhenStarted {
            viewModel.courses.collect { courses: List<CourseHeader> -> adapter!!.submitList(courses) }
        }

        viewModel.openConfirmation.observe(viewLifecycleOwner) { (title, subtitle) ->
            ConfirmDialog.newInstance(title, subtitle)
                .show(childFragmentManager, null)
        }
        viewModel.selectItem.observe(
            viewLifecycleOwner
        ) { positionWithSelectPair: Pair<Int, Boolean> ->
            selectSubjectTeacherItem(
                positionWithSelectPair.first,
                positionWithSelectPair.second
            )
        }
        viewModel.clearItemsSelection.observe(viewLifecycleOwner) { positions: Set<Int> ->
            positions.forEach { position: Int -> selectSubjectTeacherItem(position, false) }
        }
    }

    private fun selectSubjectTeacherItem(position: Int, select: Boolean) {
        val holder =
            viewBinding.rvCourse.findViewHolderForLayoutPosition(position) as CourseAdapter.CourseHolder?
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
        viewBinding.rvCourse.adapter = null
        compositeDisposable.clear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
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