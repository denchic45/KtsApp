package com.denchic45.widget.extendedAdapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.data.model.DomainModel

class DelegationAdapterExtended(
    vararg delegates: AdapterDelegate,
    private val changePayload: (DomainModel, DomainModel) -> Any?,
    private val adapterDelegatesManger: AdapterDelegatesManger = AdapterDelegatesManger(
        *delegates,
        changePayload = changePayload
    )
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    IDelegationAdapterExtended by adapterDelegatesManger {

    val adapterDelegate: SparseArrayCompat<AdapterDelegate>
        get() = adapterDelegatesManger.delegates
    override val adapterEventEmitter: AdapterEventEmitter
        get() = adapterDelegatesManger.adapterEventEmitter

    override val listItems: MutableList<Any> = adapterDelegatesManger.listItems

    init {
        adapterDelegatesManger.attachAdapter(this)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        adapterDelegatesManger.onAttachRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterDelegatesManger.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterDelegatesManger.onBindViewHolder(listItems, position, holder)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        adapterDelegatesManger.onBindViewHolder(listItems, position, holder, payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return adapterDelegatesManger.getItemViewType(listItems, position)
    }

    fun <T : AdapterDelegateExtension> addExtensions(
        vararg extensions: T
    ) {
        adapterDelegatesManger.addExtensions(*extensions)
    }

    fun addOnItemClickListener(onItemClickListener: (position: Int) -> Unit) {
        adapterDelegatesManger.addOnItemClickListener(onItemClickListener)
    }

//    override fun add(item: Any) {
//        adapterDelegatesManger.add(item)
//    }
//
//    override fun add(items: List<Any>) {
//        adapterDelegatesManger.add(items)
//    }
//
//    override fun add(item: Any, position: Int) {
//        adapterDelegatesManger.add(item, position)
//    }
//
//    override fun add(items: List<Any>, position: Int) {
//        adapterDelegatesManger.add(items,position)
//    }
//
//    override fun replace(item: Any, position: Int) {
//        adapterDelegatesManger.replace(item, position)
//    }
//
//    override fun replace(items: List<Any>, position: Int) {
//        adapterDelegatesManger.replace(items, position)
//    }
//
//    override fun remove(item: Any, position: Int) {
//        adapterDelegatesManger.remove(item, position)
//    }
//
//    override fun remove(items: List<Any>, position: Int) {
//        adapterDelegatesManger.remove(items, position)
//    }


    override fun notifyAdapterItemSetChanged() {
        notifyDataSetChanged()
    }

    override fun notifyAdapterItemChanged(position: Int, payload: Any) {
        notifyItemChanged(position, payload)
    }

    override fun notifyAdapterItemChanged(position: Int) {
        notifyItemChanged(position)
    }

    override fun notifyAdapterItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any) {
        notifyItemRangeChanged(positionStart, itemCount, payload)
    }

    override fun notifyAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        notifyItemRangeChanged(positionStart, itemCount)
    }

    override fun notifyAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun notifyAdapterItemInserted(position: Int) {
        notifyItemInserted(position)
    }

    override fun notifyAdapterItemRemoved(position: Int) {
        notifyItemRemoved(position)
    }

    override fun notifyAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        notifyItemRangeRemoved(positionStart, itemCount)
    }

    override fun notifyAdapterItemMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

//    override fun undo() {
//        adapterDelegatesManger.undo()
//    }

    override fun getItemCount(): Int = this.listItems.size

}