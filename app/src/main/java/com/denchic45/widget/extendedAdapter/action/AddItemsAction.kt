package com.denchic45.widget.extendedAdapter.action

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.widget.extendedAdapter.action.Action

class AddItemsAction(
    override val adapter: RecyclerView.Adapter<*>,
    override val currentList: MutableList<Any>,
    private val items: List<Any>,
    private val position: Int = currentList.size,
) : Action {
    override fun execute() {
        Log.d("lol", "execute: $items $position")
        currentList.addAll(position, items)
        adapter.notifyItemRangeInserted(position, items.size)
    }

    override fun undo() {
        Log.d("lol", "undo: $items $position")
        currentList.subList(position, position+items.size).clear()
        adapter.notifyItemRangeRemoved(position, items.size)
    }
}