package com.denchic45.kts.ui.course

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentCourseBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.CourseSectionAdapterDelegate
import com.denchic45.kts.ui.adapter.TaskAdapterDelegate
import com.denchic45.kts.ui.adapter.TaskHolder
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.click
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CourseFragment :
    BaseFragment<CourseViewModel, FragmentCourseBinding>(R.layout.fragment_course) {

    override val binding: FragmentCourseBinding by viewBinding(FragmentCourseBinding::bind)
    override val viewModel: CourseViewModel by viewModels { viewModelFactory }
    var collapsingToolbarLayout: CollapsingToolbarLayout? = null

    private var mainToolbar: Toolbar? = null

    companion object {
        const val COURSE_ID = "CourseFragment COURSE_UUID"
    }

    private lateinit var appBarController: AppBarController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarController = AppBarController.findController(requireActivity())
        appBarController.apply {
            mainToolbar = toolbar
            removeView(toolbar) //todo сохранять классический тулбар в переменную и возвращать при выходе
            collapsingToolbarLayout =
                addView(R.layout.toolbar_course) as CollapsingToolbarLayout
            collapsingToolbarLayout!!.findViewById<Toolbar>(R.id.toolbar).apply {
                setNavigationIcon(R.drawable.ic_arrow_back)
                inflateMenu(R.menu.options_course)
                setNavigationOnClickListener {
                    requireActivity().onBackPressed()
                }
                setOnMenuItemClickListener {
                    viewModel.onOptionClick(it.itemId)
                    false
                }
            }

            setLiftOnScroll(true)
        }

        requireActivity().findViewById<FloatingActionButton>(R.id.fab_main).setOnClickListener {
            viewModel.onFabClick()
        }

        with(binding) {
            val adapter = adapter {
                delegates(TaskAdapterDelegate(), CourseSectionAdapterDelegate())
                extensions {
                    click<TaskHolder> {
                        onClick = {
                            viewModel.onTaskItemClick(it)
                        }
                        onLongClick = {
                            viewModel.onTaskItemLongClick(it)
                            true
                        }
                    }
                }
            }
            rvCourseItems.adapter = adapter
            viewModel.showContents.observe(viewLifecycleOwner) {
                adapter.submit(it)
            }
            viewModel.courseName.observe(viewLifecycleOwner) {
                collapsingToolbarLayout!!.title = it
            }
        }

        viewModel.openTask.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_courseFragment_to_taskFragment,
                bundleOf(TaskFragment.TASK_ID to it)
            )
        }

        viewModel.openTaskEditor.observe(viewLifecycleOwner) { (taskId, courseId, sectionId) ->
            findNavController().navigate(
                R.id.action_courseFragment_to_taskEditorFragment,
                bundleOf(
                    TaskEditorFragment.TASK_ID to taskId,
                    TaskEditorFragment.COURSE_ID to courseId,
                    TaskEditorFragment.SECTION_ID to sectionId
                )
            )
        }

        viewModel.openCourseEditor.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_global_courseEditorFragment,
                bundleOf(CourseEditorFragment.COURSE_ID to it)
            )
        }

        viewModel.showMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBarController.removeView(collapsingToolbarLayout!!)
        appBarController.addView(mainToolbar)
    }
}