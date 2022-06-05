package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.EventHolder
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.ui.base.PrimaryActionModeCallback
import com.denchic45.kts.utils.Dimensions
import com.denchic45.kts.utils.closeKeyboard
import com.denchic45.kts.utils.collectWhenStarted
import com.denchic45.sample.SearchBar
import com.denchic45.widget.calendar.WeekCalendarListener
import com.denchic45.widget.calendar.model.WeekItem
import java.time.LocalDate

class TimetableFinderFragment :
    BaseFragment<TimetableFinderViewModel, FragmentTimetableFinderBinding>(
        R.layout.fragment_timetable_finder,
        R.menu.options_timetable_finder
    ) {
    override val binding: FragmentTimetableFinderBinding by viewBinding(
        FragmentTimetableFinderBinding::bind
    )
    override val viewModel: TimetableFinderViewModel by viewModels { viewModelFactory }
    private var popupWindow: ListPopupWindow? = null

    private var actionMode: PrimaryActionModeCallback = PrimaryActionModeCallback()
    private var popupAdapter: ListPopupWindowAdapter? = null


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
                        if (viewHolder is EventHolder<*> && target is EventHolder<*>) viewModel.onEventItemMove(
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
                viewModel.onEventEditItemEditClick(position)
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
                override fun onQueryTextChange(newText: String) {
                    viewModel.onGroupNameType(newText)
                }

                override fun onQueryTextSubmit(query: String) {
                    viewModel.onGroupNameType(query)
                }
            })

            rvTimetable.adapter = adapter

            viewModel.showFoundGroups.collectWhenStarted(lifecycleScope) { groups: List<ListItem> ->
                if (groups.isEmpty()) {
                    popupWindow!!.dismiss()
                    return@collectWhenStarted
                }
                popupAdapter = ListPopupWindowAdapter(requireContext(), groups)
                popupWindow!!.setAdapter(popupAdapter)
                popupWindow!!.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                    popupWindow!!.dismiss()
                    binding.searchBar.closeKeyboard()
                    searchBar.setIgnoreText(true)
                    searchBar.setText(popupAdapter!!.getItem(position).title)
                    searchBar.setIgnoreText(false)
                    viewModel.onGroupClick(position)
                }
                if (popupWindow!!.isShowing) return@collectWhenStarted
                popupWindow!!.anchorView = searchBar
                popupWindow!!.show()
                popupWindow!!.horizontalOffset = Dimensions.dpToPx(12, requireActivity())
            }

            actionMode.apply {
                onActionItemClickListener = { viewModel.onActionItemClick(it.itemId) }
                onActionModeFinish = { viewModel.onDestroyActionMode() }
            }

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

        viewModel.openEventEditor.observe(viewLifecycleOwner) {
            startActivity(Intent(requireActivity(), EventEditorActivity::class.java))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode.finishActionMode()
    }

    private fun FragmentTimetableFinderBinding.setAllowEdit(adapter: EventAdapter, allow: Boolean) {
        adapter.enableEditMode = allow
        wcv.isEnabled = !allow
        if (allow) {
            actionMode.startActionMode(binding.root, R.menu.action_timetable_editor)
        }
    }

    companion object {
        fun newInstance(): TimetableFinderFragment = TimetableFinderFragment()
    }
}