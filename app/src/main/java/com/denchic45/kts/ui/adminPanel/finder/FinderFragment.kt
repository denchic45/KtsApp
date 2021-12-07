package com.denchic45.kts.ui.adminPanel.finder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentFinderBinding
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.adapter.*
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.ui.courseEditor.CourseEditorActivity
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.group.editor.GroupEditorActivity
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.ViewUtils
import com.denchic45.widget.ListStateLayout
import com.example.appbarcontroller.appbarcontroller.AppBarController
import com.example.searchbar.SearchBar
import com.google.android.material.appbar.AppBarLayout
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FinderFragment : Fragment(R.layout.fragment_finder), OnItemClickListener,
    OnItemLongClickListener {
    private val listAdapters = listOf(
        UserAdapter(this, this),
        GroupAdapter(this, this),
        SubjectAdapter(this, this),
        SpecialtyAdapter(this),
        CourseAdapter(this, this)
    )

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<FinderViewModel>
    private val viewModel: FinderViewModel by viewModels { viewModelFactory }
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var currentAdapter: ListAdapter<DomainModel, *>
    private val navController: NavController by lazy { findNavController() }
    private lateinit var rv: RecyclerView
    private lateinit var appBarController: AppBarController
    private lateinit var rvFinderEntities: RecyclerView
    private lateinit var finderEntityAdapter: FinderEntityAdapter
    private lateinit var searchBar: SearchBar
    private val viewBinding by viewBinding(FragmentFinderBinding::bind)

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
        with(viewBinding) {
            rv = rvFoundItems
            searchBar = requireActivity().findViewById(R.id.search_bar_finder)
            finderEntityAdapter = FinderEntityAdapter()
            rvFinderEntities = appBarController.getView(R.id.rv_finder_entity)!!
            rvFinderEntities.adapter = finderEntityAdapter
            rvFinderEntities.layoutManager =
                LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            layoutManager = LinearLayoutManager(activity)
            rvFoundItems.layoutManager = layoutManager
            finderEntityAdapter.setItemClickListener { position: Int ->
                viewModel.onFinderEntitySelect(
                    position
                )
            }

            viewModel.currentSelectedEntity.observe(viewLifecycleOwner, { position: Int ->
                currentAdapter = listAdapters[position] as ListAdapter<DomainModel, *>
                rvFoundItems.adapter = currentAdapter
                AppBarController.findController(requireActivity())
                    .setExpandableIfViewCanScroll(rvFoundItems, viewLifecycleOwner)
                finderEntityAdapter.selectItem(position)
            })
        }
        searchBar.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
            override fun onQueryTextSubmit(query: String) {
                viewModel.onQueryTextSubmit(query)
            }
        })
        searchBar.attachNavigationDrawer(requireActivity().findViewById(R.id.drawer_layout))
        val listStateLayout = view as ListStateLayout
        viewModel.finderEntities.observe(
            viewLifecycleOwner,
            { list: List<ListItem> -> finderEntityAdapter.submitList(list) })

        viewModel.showListEmptyState.observe(viewLifecycleOwner, { show: Boolean ->
            if (show) {
                listStateLayout.showView(ListStateLayout.EMPTY_VIEW)
            } else {
                listStateLayout.showList()
            }
        })
        viewModel.showFoundItems.observe(
            viewLifecycleOwner,
            { foundEntities: List<DomainModel> ->
                currentAdapter.submitList(
                    foundEntities,
                    listStateLayout.getCommitCallback(currentAdapter)
                )
            })
        viewModel.openGroup.observe(viewLifecycleOwner, { groupUuid: String? ->
            val bundle = Bundle()
            bundle.putString(GroupFragment.GROUP_UUID, groupUuid)
            navController.navigate(R.id.action_finderFragment_to_group_editor, bundle)
        })
        viewModel.showMessage.observe(viewLifecycleOwner, { message: String? ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        })
        viewModel.openUserEditor.observe(
            viewLifecycleOwner,
            { args: Map<String, String> ->
                val intent = Intent(
                    activity, UserEditorActivity::class.java
                )
                args.forEach { (name: String?, value: String?) -> intent.putExtra(name, value) }
                startActivity(intent)
            })
        viewModel.openProfile.observe(viewLifecycleOwner, { userUuid: String? ->
            val bundle = Bundle()
            bundle.putString(ProfileFragment.USER_UUID, userUuid)
            navController.navigate(R.id.action_global_profileFragment, bundle)
        })
        viewModel.openSubjectEditor.observe(viewLifecycleOwner, { subjectUuid: String? ->
            SubjectEditorDialog.newInstance(subjectUuid).show(
                childFragmentManager, null
            )
        })
        viewModel.openGroupEditor.observe(viewLifecycleOwner, { groupUuid: String? ->
            val intent = Intent(activity, GroupEditorActivity::class.java)
            intent.putExtra(GroupEditorActivity.GROUP_UUID, groupUuid)
            requireActivity().startActivity(intent)
        })
        viewModel.openSpecialtyEditor.observe(viewLifecycleOwner, { uuid: String? ->
            SpecialtyEditorDialog.newInstance(uuid).show(
                childFragmentManager, null
            )
        })
        viewModel.openConfirmation.observe(viewLifecycleOwner, { (first, second) ->
            ConfirmDialog.newInstance(
                first, second
            ).show(childFragmentManager, null)
        })

        viewModel.openCourse.observe(viewLifecycleOwner) {
            navController.navigate(
                R.id.action_finderFragment_to_courseEditorFragment,
                bundleOf(CourseEditorActivity.COURSE_UUID to it)
            )
        }
        viewModel.showOptions.observe(viewLifecycleOwner, { (first, second) ->
            val popupWindow = ListPopupWindow(
                requireActivity()
            )
            val popupAdapter = ListPopupWindowAdapter(activity, second)
            popupWindow.anchorView = layoutManager.findViewByPosition(first)
            popupWindow.setOnItemClickListener { parent: AdapterView<*>?, view1: View?, position: Int, id: Long ->
                popupWindow.dismiss()
                viewModel.onOptionClick(second[position].uuid)
            }
            popupWindow.width = ViewUtils.measureAdapter(popupAdapter, activity)
            popupWindow.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
            popupWindow.setAdapter(popupAdapter)
            popupWindow.show()
        })
    }

    override fun onItemClick(position: Int) {
        viewModel.onFinderItemClick(position)
    }

    override fun onLongItemClick(position: Int) {
        viewModel.onFinderItemLongClick(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showToolbar()
        rv.adapter = null
    }
}