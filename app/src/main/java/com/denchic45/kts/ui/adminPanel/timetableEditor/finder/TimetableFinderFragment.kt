package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentTimetableFinderBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.EventHolder
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.widget.calendar.WeekCalendarListener
import com.denchic45.widget.calendar.model.WeekItem
import com.example.searchbar.SearchBar
import java.time.LocalDate

class TimetableFinderFragment :
    BaseFragment<TimetableFinderViewModel, FragmentTimetableFinderBinding>(
        R.layout.fragment_timetable_finder
    ) {
    override val binding: FragmentTimetableFinderBinding by viewBinding(
        FragmentTimetableFinderBinding::bind
    )
    override val viewModel: TimetableFinderViewModel by viewModels { viewModelFactory }
    private var popupWindow: ListPopupWindow? = null

    //    private var adapter: EventAdapter? = null
    private var actionMode: ActionMode? = null
    private var popupAdapter: ListPopupWindowAdapter? = null

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_timetable_finder, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val simpleCallback: ItemTouchHelper.SimpleCallback
        get() {
            val simpleCallback: ItemTouchHelper.SimpleCallback =
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        if (viewHolder is EventHolder<*> && target is EventHolder<*>) viewModel.onLessonItemMove(
                            viewHolder.getAbsoluteAdapterPosition(),
                            target.getAbsoluteAdapterPosition()
                        )
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
                    override fun isLongPressDragEnabled(): Boolean {
                        return false
                    }
                }
            return simpleCallback
        }

    private val itemTouchHelper = ItemTouchHelper(simpleCallback)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popupWindow = ListPopupWindow(requireActivity())

        val adapter = EventAdapter(
            viewModel.lessonTime, false,
            onCreateLessonClickListener = { viewModel.onCreateEventItemClick() },
            onEditEventItemClickListener = { position, _ ->
                viewModel.onLessonItemEditClick(position)
            },
            onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
                itemTouchHelper.startDrag(
                    viewHolder
                )
            }
        )

        with(binding) {
            wcv.weekCalendarListener = object : WeekCalendarListener {
                override fun onDaySelect(date: LocalDate) {
                    viewModel.onDateSelect(date)
                }

                override fun onWeekSelect(weekItem: WeekItem) {}
            }

            searchBar.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
                override fun onQueryTextChange(groupName: String) {
                    viewModel.onGroupNameType(groupName)
                }

                override fun onQueryTextSubmit(groupName: String) {
                    viewModel.onGroupNameType(groupName)
                }
            })

            rvTimetable.adapter = adapter

            viewModel.showFoundGroups.observe(viewLifecycleOwner) { groups: List<ListItem> ->
                if (groups.isEmpty()) {
                    popupWindow!!.dismiss()
                    return@observe
                }
                popupAdapter = ListPopupWindowAdapter(requireContext(), groups)
                popupWindow!!.setAdapter(popupAdapter)
                popupWindow!!.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    popupWindow!!.dismiss()
                    searchBar.setIgnoreText(true)
                    searchBar.setText(popupAdapter!!.getItem(position).title)
                    searchBar.setIgnoreText(false)
                    viewModel.onGroupClick(position)
                }
                if (popupWindow!!.isShowing) return@observe
                popupWindow!!.anchorView = searchBar
                popupWindow!!.show()
                popupWindow!!.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
            }

//            viewModel.enableEditMode.observe(viewLifecycleOwner) { allow: Boolean ->
//                setAllowEdit(allow)
//            }
//            viewModel.showEditedLessons.observe(viewLifecycleOwner) { lessons: List<DomainModel> ->
//                adapter!!.submitList(ArrayList(lessons))
//            }

            viewModel.eventsOfDay.collectWhenStarted(lifecycleScope) { eventsOfDayState ->
                when (eventsOfDayState) {
                    is TimetableFinderViewModel.EventsOfDayState.Current -> {
                        setAllowEdit(adapter, false)
                    }
                    is TimetableFinderViewModel.EventsOfDayState.Edit -> {
                        setAllowEdit(adapter, true)
                    }
                }

                adapter.submitList(eventsOfDayState.events)
            }

            viewModel.editTimetableOptionVisibility.observe(
                viewLifecycleOwner
            ) { visible: Boolean ->
                menu.getItem(0).isVisible = visible
            }

            itemTouchHelper.attachToRecyclerView(rvTimetable)
        }

//        viewModel.eventsOfDay.collectWhenStarted(
//            lifecycleScope
//        ) { lessons ->
//            adapter!!.submitList(ArrayList<DomainModel>(lessons))
//        }

        viewModel.openEventEditor.observe(viewLifecycleOwner) {
            startActivity(Intent(requireActivity(), EventEditorActivity::class.java))
        }

    }

    private fun FragmentTimetableFinderBinding.setAllowEdit(adapter: EventAdapter, allow: Boolean) {
        adapter.enableEditMode = allow
        if (allow) {
            wcv.isEnabled = false

            actionMode = requireActivity().startActionMode(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    mode.menuInflater.inflate(R.menu.action_timetable_editor, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return true
                }

                override fun onActionItemClicked(
                    mode: ActionMode,
                    item: MenuItem
                ): Boolean {
                    viewModel.onActionItemClick(item.itemId)
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    viewModel.onDestroyActionMode()
                    actionMode = null
                    wcv.isEnabled = true
                }
            })
        } else if (actionMode != null) {
            actionMode!!.finish()
        }
    }

    companion object {
        fun newInstance(): TimetableFinderFragment {
            return TimetableFinderFragment()
        }
    }
}