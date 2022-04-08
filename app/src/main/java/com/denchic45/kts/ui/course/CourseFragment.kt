package com.denchic45.kts.ui.course

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentCourseBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.HasNavArgs
import com.denchic45.kts.ui.adapter.CourseSectionAdapterDelegate
import com.denchic45.kts.ui.adapter.TaskAdapterDelegate
import com.denchic45.kts.ui.adapter.TaskHolder
import com.denchic45.kts.ui.course.content.ContentFragment
import com.denchic45.kts.ui.course.sections.CourseSectionEditorFragment
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.kts.utils.collectWhenResumed
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.kts.utils.dpToPx
import com.denchic45.kts.utils.toast
import com.denchic45.widget.extendedAdapter.adapter
import com.denchic45.widget.extendedAdapter.extension.clickBuilder
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CourseFragment : BaseFragment<CourseViewModel, FragmentCourseBinding>(
    R.layout.fragment_course
), HasNavArgs<CourseFragmentArgs> {

    override val navArgs: CourseFragmentArgs by navArgs()

    override val binding: FragmentCourseBinding by viewBinding(FragmentCourseBinding::bind)
    override val viewModel: CourseViewModel by viewModels { viewModelFactory }
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    private lateinit var mainToolbar: Toolbar
    private lateinit var toolbar: Toolbar

    companion object {
        const val COURSE_ID = "CourseFragment COURSE_UUID"
    }

    private lateinit var appBarController: AppBarController

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarController = AppBarController.findController(requireActivity())

        requireActivity().findViewById<FloatingActionButton>(R.id.fab_main).setOnClickListener {
            viewModel.onFabClick()
        }

        with(binding) {
            val adapter = adapter {
                delegates(TaskAdapterDelegate(), CourseSectionAdapterDelegate())
                extensions {
                    clickBuilder<TaskHolder> {
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

            val simpleCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val move = viewHolder is TaskHolder && target is TaskHolder
                    if (move) {
                        val oldPosition = viewHolder.absoluteAdapterPosition
                        val position = target.absoluteAdapterPosition
                        viewModel.onContentMove(oldPosition, position)
                    }
                    return move
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    viewHolder.itemView.isSelected =
                        actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewModel.onContentMoved()
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
            val itemTouchHelper = ItemTouchHelper(simpleCallback)

            itemTouchHelper.attachToRecyclerView(rvCourseItems)

            rvCourseItems.adapter = adapter
            viewModel.showContents.observe(viewLifecycleOwner) {
                adapter.submit(it.toList())
            }

            viewModel.courseName.flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).collectWhenStarted(lifecycleScope) {
                collapsingToolbarLayout.title = it
            }


            viewModel.fabVisibility.observe(viewLifecycleOwner) {
                requireActivity().findViewById<FloatingActionButton>(R.id.fab_main)
                    .apply {
                        if (it)
                            show()
                        else
                            hide()
                    }
            }
        }

        viewModel.openTask.observe(viewLifecycleOwner) { (taskId, courseId) ->
            navController.navigate(
                R.id.action_courseFragment_to_contentFragment,
                bundleOf(ContentFragment.TASK_ID to taskId, ContentFragment.COURSE_ID to courseId)
            )
        }

        viewModel.openTaskEditor.observe(viewLifecycleOwner) { (taskId, courseId, sectionId) ->
            navController.navigate(
                R.id.action_courseFragment_to_taskEditorFragment,
                bundleOf(
                    TaskEditorFragment.TASK_ID to taskId,
                    TaskEditorFragment.COURSE_ID to courseId,
                    TaskEditorFragment.SECTION_ID to sectionId
                )
            )
        }

        viewModel.openCourseEditor.observe(viewLifecycleOwner) {
            navController.navigate(
                R.id.action_global_courseEditorFragment,
                bundleOf(CourseEditorFragment.COURSE_ID to it)
            )
        }

        viewModel.openCourseSectionEditor.observe(viewLifecycleOwner) {
            navController.navigate(
                R.id.action_courseFragment_to_courseSectionsFragment,
                bundleOf(CourseSectionEditorFragment.COURSE_ID to it)
            )
        }
    }

    override fun collectOnOptionVisibility() {
        viewModel.optionsVisibility.collectWhenStarted(lifecycleScope) { optionsVisibility ->
            optionsVisibility.forEach { (itemId, visible) ->
                toolbar.menu.findItem(itemId).isVisible = visible
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appBarController.apply {
            mainToolbar = toolbar
            removeView(toolbar)
            collapsingToolbarLayout =
                addView(R.layout.toolbar_course) as CollapsingToolbarLayout
            this@CourseFragment.toolbar = collapsingToolbarLayout.findViewById(R.id.toolbar)
            this@CourseFragment.toolbar.apply {
                setNavigationIcon(R.drawable.ic_arrow_back)
                inflateMenu(R.menu.options_course)
                setNavigationOnClickListener {
                    requireActivity().onBackPressed()
                }
                setOnMenuItemClickListener {
                    viewModel.onOptionClick(it.itemId)
                    false
                }
                viewModel.onCreateOptions()
            }
            setLiftOnScroll(true)
        }
    }

    override fun onStop() {
        super.onStop()
        appBarController.removeView(collapsingToolbarLayout)
        appBarController.addView(mainToolbar)
        appBarController.setExpanded(expand = true, animate = false)
    }
}