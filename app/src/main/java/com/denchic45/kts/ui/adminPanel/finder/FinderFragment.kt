package com.denchic45.kts.ui.adminPanel.finder

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentFinderBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.utils.*
import com.denchic45.sample.SearchBar
import com.denchic45.widget.ListStateLayout
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.drop

class FinderFragment :
    BaseFragment<FinderViewModel, FragmentFinderBinding>(R.layout.fragment_finder),
    OnItemClickListener,
    OnItemLongClickListener {
    private val listAdapters = listOf(
        UserAdapter(this, this),
        GroupAdapter(this, this),
        SubjectAdapter(this, this),
        SpecialtyAdapter(this),
        CourseAdapter(this, this)
    )

    override val viewModel: FinderViewModel by viewModels { viewModelFactory }

    private lateinit var currentAdapter: ListAdapter<DomainModel, *>

    private lateinit var appBarController: AppBarController
    private lateinit var rvFinderEntities: RecyclerView
    private lateinit var finderEntityAdapter: FinderEntityAdapter
    private lateinit var searchBar: SearchBar
    override val binding by viewBinding({ FragmentFinderBinding.bind(it) })

    private fun hideMainToolbar() {
        appBarController.toolbar.visibility = View.GONE
        appBarController.addView(R.layout.searchbar_appbar)
        appBarController.addView(
            R.layout.rv_finder_entity,
            AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        )
    }

    private fun showToolbar() {
        appBarController.removeView(rvFinderEntities)
        appBarController.removeView(searchBar)
        searchBar.detachNavigationDrawer()
        appBarController.toolbar.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appBarController = AppBarController.findController(requireActivity())
        hideMainToolbar()
        with(binding) {
            searchBar = requireActivity().findViewById(R.id.search_bar_finder)
            finderEntityAdapter = FinderEntityAdapter()
            rvFinderEntities = appBarController.getView(R.id.rv_finder_entity)!!
            rvFinderEntities.adapter = finderEntityAdapter
            rvFinderEntities.layoutManager =
                LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            finderEntityAdapter.setItemClickListener { position: Int ->
                viewModel.onFinderEntitySelect(position)
            }

            viewModel.currentSelectedEntity.collectWhenStarted(lifecycleScope) { position: Int ->
                currentAdapter = listAdapters[position] as ListAdapter<DomainModel, *>
                rvFoundItems.adapter = currentAdapter
                AppBarController.findController(requireActivity())
                    .setExpandableIfViewCanScroll(rvFoundItems, viewLifecycleOwner)
                finderEntityAdapter.selectItem(position)
            }
        }
        searchBar.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
            override fun onQueryTextSubmit(query: String) {
                viewModel.onQueryTextSubmit(query)
            }
        })
        searchBar.attachNavigationDrawer(requireActivity().findViewById(R.id.drawer_layout))
        val listStateLayout = view as ListStateLayout
        viewModel.finderEntities.observe(
            viewLifecycleOwner
        ) { list: List<ListItem> -> finderEntityAdapter.submitList(list) }

        viewModel.foundItems
            .drop(1)
            .collectWhenStarted(lifecycleScope) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        listStateLayout.showList()
                        currentAdapter.submitList(
                            resource.data,
                            listStateLayout.getCommitCallback(currentAdapter)
                        )
                    }
                    Resource.Loading -> {}
                    is Resource.Error -> {
                        when (resource.error) {
                            is NetworkException -> {
                                listStateLayout.showView(ListStateLayout.NETWORK_VIEW)
                            }
                            else -> toast("Ошибка: ${resource.error}")
                        }
                    }
                    is Resource.Next -> throw IllegalStateException()
                }
            }

        viewModel.openSubjectEditor.observe(viewLifecycleOwner) { subjectId: String ->
            navController.navigate(
                R.id.action_global_subjectEditorDialog,
                bundleOf(SubjectEditorDialog.SUBJECT_ID to subjectId)
            )
        }
        viewModel.openGroupEditor.observe(viewLifecycleOwner) { groupId ->
            navController.navigate(
                R.id.action_global_groupEditorFragment,
                bundleOf(GroupEditorFragment.GROUP_ID to groupId)
            )
        }
        viewModel.openSpecialtyEditor.observe(viewLifecycleOwner) { id ->
            SpecialtyEditorDialog.newInstance(id).show(
                childFragmentManager, null
            )
        }

        viewModel.openCourse.observe(viewLifecycleOwner) {
            navController.navigate(
                R.id.action_global_courseFragment,
                bundleOf(CourseFragment.COURSE_ID to it)
            )
        }
        viewModel.showOptions.observe(viewLifecycleOwner) { (first, second) ->
            val popupWindow = ListPopupWindow(
                requireActivity()
            )
            val popupAdapter = ListPopupWindowAdapter(requireContext(), second)
            popupWindow.anchorView = binding.rvFoundItems.layoutManager!!.findViewByPosition(first)
            popupWindow.setOnItemClickListener { _, _, position, _ ->
                popupWindow.dismiss()
                viewModel.onOptionClick(second[position].id)
            }
            popupWindow.width = ViewUtils.measureAdapter(popupAdapter, requireContext())
            popupWindow.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
            popupWindow.setAdapter(popupAdapter)
            popupWindow.show()
        }
    }

    override fun onItemClick(position: Int) {
        viewModel.onFinderItemClick(position)
    }

    override fun onLongItemClick(position: Int) {
        viewModel.onFinderItemLongClick(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showToolbar()
    }
}