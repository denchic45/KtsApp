package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.denchic45.EventObserver
import com.denchic45.kts.R
import com.denchic45.kts.customPopup.ListPopupWindowAdapter
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.databinding.FragmentTimetableFinderBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.EventHolder
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.utils.Dimensions
import com.denchic45.widget.ListStateLayout
import com.denchic45.widget.calendar.WeekCalendarListener
import com.denchic45.widget.calendar.WeekCalendarView
import com.denchic45.widget.calendar.model.Week
import com.example.searchbar.SearchBar
import org.jetbrains.annotations.Contract
import java.util.*

class TimetableFinderFragment :
    BaseFragment<TimetableFinderViewModel, FragmentTimetableFinderBinding>() {
    override val binding: FragmentTimetableFinderBinding by viewBinding(
        FragmentTimetableFinderBinding::bind
    )
    override val viewModel: TimetableFinderViewModel by viewModels { viewModelFactory }
    private lateinit var searchBar: SearchBar
    private var popupWindow: ListPopupWindow? = null
    private var rv: RecyclerView? = null
    private var adapter: EventAdapter? = null
    private lateinit var menu: Menu
    private var actionMode: ActionMode? = null
    private lateinit var wcv: WeekCalendarView
    private var listStateLayout: ListStateLayout? = null
    private var popupAdapter: ListPopupWindowAdapter? = null

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_timetable_finder, container, false)
        searchBar = root.findViewById(R.id.sb_group)
        rv = root.findViewById(R.id.rv_timetable)
        wcv = root.findViewById(R.id.wcv)
        listStateLayout = root.findViewById(R.id.listStateLayout)
        wcv.setListener(object : WeekCalendarListener {
            override fun onDaySelect(date: Date) {
                viewModel.onDateSelect(date)
            }

            override fun onWeekSelect(week: Week) {}
        })
        searchBar.setOnQueryTextListener(object : SearchBar.OnQueryTextListener() {
            override fun onQueryTextChange(groupName: String) {
                viewModel.onGroupNameType(groupName)
            }

            override fun onQueryTextSubmit(groupName: String) {
                viewModel.onGroupNameType(groupName)
            }
        })
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        menu.clear()
        inflater.inflate(R.menu.options_timetable_finder, menu)
        viewModel.onCreateOptions()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onOptionClick(item.itemId)
        return super.onOptionsItemSelected(item)
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
        adapter = EventAdapter(
            viewModel.lessonTime, false,
            onCreateLessonClickListener = { viewModel.onCreateEventItemClick() },
            onEditEventItemClickListener = { position ->
                viewModel.onLessonItemEditClick(
                    position
                )
            },
            onItemTouchListener = { viewHolder: RecyclerView.ViewHolder? ->
                itemTouchHelper.startDrag(
                    viewHolder!!
                )
            }
        )
        rv!!.adapter = adapter
        viewModel.showLessonsOfGroupByDate.observe(
            viewLifecycleOwner,
            EventObserver { lessons: List<Event> ->
                Log.d("lol1", "showLessonsOfGroupByDate: ")
                adapter!!.submitList(ArrayList<DomainModel>(lessons))
            })
        viewModel.openEventEditor.observe(viewLifecycleOwner) {
            startActivity(
                Intent(
                    activity, EventEditorActivity::class.java
                )
            )
        }
        viewModel.showMessageRes.observe(viewLifecycleOwner) { resId: Int ->
            Toast.makeText(
                context, getString(
                    resId
                ), Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.enableEditMode.observe(viewLifecycleOwner) { allow: Boolean ->
            if (allow) {
                wcv.isEnabled = false
                adapter!!.enableEditMode()
                actionMode = requireActivity().startActionMode(object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        mode.menuInflater.inflate(R.menu.action_timetable_editor, menu)
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
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
                adapter!!.disableEditMode()
                actionMode!!.finish()
            }
        }
        viewModel.showEditedLessons.observe(viewLifecycleOwner) { lessons: List<DomainModel> ->
            Log.d("lol1", "showEditedLessons: ")
            adapter!!.submitList(ArrayList(lessons))
        }
        viewModel.editTimetableOptionVisibility.observe(
            viewLifecycleOwner
        ) { visible: Boolean -> menu.getItem(0).isVisible = visible }

        itemTouchHelper.attachToRecyclerView(rv)
    }

    companion object {
        @Contract(" -> new")
        fun newInstance(): TimetableFinderFragment {
            return TimetableFinderFragment()
        }
    }
}