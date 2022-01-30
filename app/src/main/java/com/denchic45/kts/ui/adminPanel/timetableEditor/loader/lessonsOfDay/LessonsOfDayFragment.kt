package com.denchic45.kts.ui.adminPanel.timetableEditor.loader.lessonsOfDay

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.EventAdapter.*
import com.denchic45.kts.ui.adapter.OnItemMoveListener
import com.denchic45.kts.ui.base.listFragment.ListFragment

//TODO НЕ ПЕРЕДАВАТЬ СЛУШАТЕЛИ ЧЕРЕЗ КОНСТРУКТОР!!!
class LessonsOfDayFragment : ListFragment<EventAdapter>() {



    private val adapter: EventAdapter = EventAdapter(40, false,
        onItemTouchListener =  { viewHolder: RecyclerView.ViewHolder? ->
          itemTouchHelper.startDrag(
              viewHolder!!
          )
      }
    )
    private var onItemMoveListener: OnItemMoveListener? = null
    fun setList(list: List<DomainModel>) {
        adapter.submitList(ArrayList(list))
    }

    fun setOnLessonItemClickListener(onLessonItemClickListener: OnLessonItemClickListener?) {
        adapter.onLessonItemClickListener = onLessonItemClickListener!!
    }

    fun setOnCreateLessonClickListener(onCreateLessonClickListener: OnCreateLessonClickListener?) {
        adapter.onCreateLessonClickListener = onCreateLessonClickListener!!
    }

    fun setOnEditEventItemClickListener(onEditEventItemClickListener: OnEditEventItemClickListener) {
        adapter.onEditEventItemClickListener = onEditEventItemClickListener
    }

    fun setOnItemMoveListener(onItemMoveListener: OnItemMoveListener?) {
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
                        if (viewHolder is EventHolder<*> && target is EventHolder<*>) onItemMoveListener!!.onMove(
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

    val itemTouchHelper = ItemTouchHelper(simpleCallback1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(adapter)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun enableEditMode() {
        adapter.enableEditMode()
    }

    fun disableEditMode() {
        adapter.disableEditMode()
    }

    fun updateList(list: List<DomainModel>?) {
        adapter.submitList(ArrayList(list))
    }

}