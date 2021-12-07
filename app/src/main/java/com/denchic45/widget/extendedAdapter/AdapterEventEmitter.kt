package com.denchic45.widget.extendedAdapter

import androidx.recyclerview.widget.RecyclerView

class AdapterEventEmitter : IEVentEmitter {

    private val onCreateObservers: MutableList<(RecyclerView.ViewHolder) -> Unit> = mutableListOf()
    private val onItemClickObservers: MutableList<(RecyclerView.ViewHolder) -> Unit> =
        mutableListOf()
    private val onItemLongClickObservers: MutableList<(RecyclerView.ViewHolder) -> Unit> =
        mutableListOf()
    private val onBindObservers: MutableList<(RecyclerView.ViewHolder) -> Unit> = mutableListOf()
    private val onNotifyItemRemovedObservers: MutableList<(positionStart: Int, itemCount: Int) -> Unit> =
        mutableListOf()
    private val onNotifyItemInsertedObservers: MutableList<(positionStart: Int, itemCount: Int) -> Unit> =
        mutableListOf()


    override fun addOnCreateObserver(onCreateObserver: (RecyclerView.ViewHolder) -> Unit) {
        onCreateObservers.add(onCreateObserver)
    }

    override fun addOnBindObserver(onBindObserver: (RecyclerView.ViewHolder) -> Unit) {
        onBindObservers.add(onBindObserver)
    }

    override fun addOnItemClickObserver(onItemClickObserver: (RecyclerView.ViewHolder) -> Unit) {
        onItemClickObservers.add(onItemClickObserver)
    }

    override fun addOnItemLongClickObserver(onItemLongClickObserver: (RecyclerView.ViewHolder) -> Unit) {
        onItemLongClickObservers.add(onItemLongClickObserver)
    }

    override fun addOnNotifyItemRemovedObserver(onNotifyItemRemovedObserver: (positionStart: Int, itemCount: Int) -> Unit) {
        onNotifyItemRemovedObservers.add(onNotifyItemRemovedObserver)
    }

    override fun addOnNotifyItemInsertedObserver(onNotifyItemRemovedObserver: (position: Int, itemCount: Int) -> Unit) {
        onNotifyItemInsertedObservers.add(onNotifyItemRemovedObserver)
    }

    fun onBindViewHolderEvent(holder: RecyclerView.ViewHolder) {
        onBindObservers.forEach { it(holder) }
    }

    fun onCreateViewHolderEvent(holder: RecyclerView.ViewHolder) {
        fun onSetItemClickListener() {
            holder.itemView.setOnClickListener {
                onItemClickObservers.forEach { it(holder) }
            }
        }

        fun onSetItemLongClickListener() {
            holder.itemView.setOnLongClickListener {
                onItemLongClickObservers.forEach { it(holder) }
                true
            }
        }

        onSetItemClickListener()
        onSetItemLongClickListener()
        onCreateObservers.forEach { it(holder) }
    }



    override fun onNotifyAdapterItemChanged(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onNotifyAdapterItemChanged(positionStart: Int, itemCount: Int, payload: Any) {
        TODO("Not yet implemented")
    }

    override fun onNotifyAdapterItemChanged(positionStart: Int, itemCount: Int) {
        TODO("Not yet implemented")
    }

    override fun onNotifyAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        TODO("Not yet implemented")
    }

    override fun onNotifyAdapterItemInserted(positionStart: Int, itemCount: Int) {
        onNotifyItemInsertedObservers.forEach { it(positionStart, itemCount) }
    }

    override fun onNotifyAdapterItemRemoved(positionStart: Int, itemCount: Int) {
        onNotifyItemRemovedObservers.forEach { it(positionStart, itemCount) }
    }

    override fun onNotifyAdapterItemMoved(positionStart: Int, itemCount: Int) {

    }
}