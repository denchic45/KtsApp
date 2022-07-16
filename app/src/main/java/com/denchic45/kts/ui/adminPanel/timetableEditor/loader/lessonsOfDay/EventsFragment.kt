package com.denchic45.kts.ui.adminPanel.timetableEditor.loader.lessonsOfDay

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.*
import com.denchic45.kts.ui.adapter.OnItemMoveListener
import com.denchic45.kts.ui.base.listFragment.ListFragment
import kotlin.math.abs

class EventsFragment : ListFragment<ConcatAdapter>() {

    private var dayOfWeekOfMovedEvents: Int = 0

    private val mondayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 1
        }
    )

    private val tuesdayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 2
        }
    )

    private val wednesdayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 3
        }
    )

    private val thursdayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 4
        }
    )

    private val fridayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 5
        }
    )

    private val saturdayEventAdapter: EventAdapter = EventAdapter(40, false,
        onItemMoveListener = { viewHolder: RecyclerView.ViewHolder ->
            itemTouchHelper.startDrag(viewHolder)
            dayOfWeekOfMovedEvents = 6
        }
    )

    private val adapters = listOf(
        mondayEventAdapter,
        tuesdayEventAdapter,
        wednesdayEventAdapter,
        thursdayEventAdapter,
        fridayEventAdapter,
        saturdayEventAdapter
    )

    private val concatAdapter: ConcatAdapter = ConcatAdapter(adapters)

    private var onItemMoveListener: OnItemMoveListener? = null

    fun setEditMode(editMode: Boolean) {
        adapters.forEach { it.enableEditMode = editMode }
    }

    fun submitList(list: List<List<DomainModel>>) {
        list.withIndex().forEach { (i, events) ->
            adapters[i].submitList(events)
        }
    }

    fun submitList(list: List<DomainModel>, dayOfWeek: Int) {
        adapters[dayOfWeek].submitList(list)
    }

    fun setOnLessonItemClickListener(onLessonItemClickListener: OnLessonItemClickListener) {
        adapters.forEach {
            it.onLessonItemClickListener = onLessonItemClickListener
        }

    }

    fun setOnCreateLessonClickListener(onCreateLessonClickListener: OnCreateLessonClickListener) {
        adapters.forEach {
            it.onCreateLessonClickListener = onCreateLessonClickListener
        }
    }

    fun setOnEditEventItemClickListener(onEditEventItemClickListener: OnEditEventItemClickListener) {
        adapters.forEach {
            it.onEditEventItemClickListener = onEditEventItemClickListener
        }
    }

    fun setOnItemMoveListener(onItemMoveListener: OnItemMoveListener) {
        this.onItemMoveListener = onItemMoveListener
    }

    private val simpleCallback1: ItemTouchHelper.SimpleCallback
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
                        if (viewHolder is EventHolder<*> && target is EventHolder<*>) {
                            if (abs(viewHolder.absoluteAdapterPosition - target.absoluteAdapterPosition) <= 1) {
                                onItemMoveListener!!.onMove(
                                    viewHolder.bindingAdapterPosition,
                                    target.bindingAdapterPosition,
                                    dayOfWeekOfMovedEvents
                                )
                                return false
                            }
                        }
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
                    override fun isLongPressDragEnabled(): Boolean {
                        return false
                    }
                }
            return simpleCallback
        }

    private val itemTouchHelper = ItemTouchHelper(simpleCallback1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = concatAdapter
        itemTouchHelper.attachToRecyclerView(binding.rv)
    }

    fun setEnableEditMode(editMode: Boolean) {
        adapters.forEach {
            it.enableEditMode = editMode
        }
    }
}