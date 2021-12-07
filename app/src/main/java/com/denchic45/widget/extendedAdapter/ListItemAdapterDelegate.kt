package com.denchic45.widget.extendedAdapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class ListItemAdapterDelegate<I : Any, VH : RecyclerView.ViewHolder> : AdapterDelegate {

    override fun isForViewType(items: List<Any>, position: Int): Boolean {
        return isForViewType(items[position])
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        items: List<Any>,
        position: Int
    ) {
        onBindViewHolder(items[position] as I, holder as VH)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        items: List<Any>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty())
            payloads.forEach { onBindViewHolder(items[position] as I, holder as VH, it) }
        else
            onBindViewHolder(items[position] as I, holder as VH)
    }

    abstract fun isForViewType(item: Any): Boolean

    abstract fun onBindViewHolder(item: I, holder: VH)

    open fun onBindViewHolder(item: I, holder: VH, payload: Any) {

    }

    abstract override fun onCreateViewHolder(parent: ViewGroup): VH


}