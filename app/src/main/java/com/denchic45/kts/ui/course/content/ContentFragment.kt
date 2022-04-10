package com.denchic45.kts.ui.course.content

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentContentBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.course.submissions.SubmissionsFragment
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import com.denchic45.kts.ui.course.taskInfo.TaskInfoFragment
import com.denchic45.kts.utils.setActivityTitle
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.distinctUntilChanged

class ContentFragment :
    BaseFragment<ContentViewModel, FragmentContentBinding>(
        R.layout.fragment_content,
        R.menu.options_course_content
    ) {
    override val viewModel: ContentViewModel by viewModels { viewModelFactory }
    override val binding: FragmentContentBinding by viewBinding(FragmentContentBinding::bind)

    private val appBarController by lazy {
        AppBarController.findController(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarController.setExpanded(expand = true, animate = true)
        setActivityTitle("")

        with(binding) {

            lifecycleScope.launchWhenStarted {
                viewModel.submissionVisibility
                    .distinctUntilChanged()
                    .collect {
                        vpContent.isUserInputEnabled = it
                        val adapter: ContentAdapter
                        if (it) {
                            tabLayout.visibility = View.VISIBLE
                            adapter = ContentAdapter(this@ContentFragment, 2)
                        } else {
                            tabLayout.visibility = View.GONE
                            adapter = ContentAdapter(this@ContentFragment, 1)
                        }
                        vpContent.adapter = adapter
                        TabLayoutMediator(tabLayout, vpContent) { tab, position ->
                            tab.text = viewModel.tabNames[position]
                        }.attach()
                    }
            }
        }

        viewModel.openTaskEditor.observe(viewLifecycleOwner) { (taskId, courseId) ->
            findNavController().navigate(
                R.id.action_global_taskEditorFragment,
                bundleOf(
                    TaskEditorFragment.TASK_ID to taskId,
                    TaskEditorFragment.COURSE_ID to courseId
                )
            )
        }
    }

    companion object {
        const val TASK_ID = "TASK_ID"
        const val COURSE_ID = "COURSE_ID"
    }

    class ContentAdapter(fragment: Fragment, private val fragmentCount: Int) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount() = fragmentCount

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TaskInfoFragment()
                1 -> SubmissionsFragment()
                else -> throw IllegalStateException()
            }
        }
    }
}