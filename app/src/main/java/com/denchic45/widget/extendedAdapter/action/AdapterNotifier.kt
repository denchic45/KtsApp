package com.denchic45.widget.extendedAdapter.action

interface AdapterNotifier {

    fun notifyAdapterItemChanged(position: Int, payload: Any)

    fun notifyAdapterItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any)

    fun notifyAdapterItemChanged(position: Int)

    fun notifyAdapterItemRangeChanged(positionStart: Int, itemCount: Int)

    fun notifyAdapterItemRangeInserted(positionStart: Int, itemCount: Int)

    fun notifyAdapterItemSetChanged()

    fun notifyAdapterItemInserted(position: Int)

    fun notifyAdapterItemRemoved(position: Int)

    fun notifyAdapterItemRangeRemoved(positionStart: Int, itemCount: Int)

    fun notifyAdapterItemMoved(fromPosition: Int, toPosition: Int)
}