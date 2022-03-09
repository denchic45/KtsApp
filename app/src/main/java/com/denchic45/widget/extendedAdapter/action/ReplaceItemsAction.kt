package com.denchic45.widget.extendedAdapter.action

import androidx.recyclerview.widget.RecyclerView

class ReplaceItemsAction(
    override val adapter: RecyclerView.Adapter<*>,
    override val currentList: MutableList<Any>,
    private val items: List<Any>,
    private val position: Int
) : Action {
    lateinit var oldItems: List<Any>
    override fun execute() {
        oldItems = currentList.slice(position until position+items.size)
        repeat(oldItems.size) {
            currentList.removeAt(position)
        }

        currentList.addAll(position,items)
        adapter.notifyItemRangeChanged(position, items.size)
    }

    override fun undo() {
        val newItems = currentList.subList(position, position + items.size)
        repeat(oldItems.size) {
            currentList.removeAt(position)
        }
        currentList.addAll(position,oldItems)
        adapter.notifyItemRangeChanged(position, oldItems.size)
    }
}