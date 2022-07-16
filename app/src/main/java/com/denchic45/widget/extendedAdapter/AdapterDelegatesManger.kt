package com.denchic45.widget.extendedAdapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.kts.domain.DomainModel
import com.denchic45.widget.extendedAdapter.action.ActionHistory


class AdapterDelegatesManger(
    vararg delegates: AdapterDelegate,
    override val adapterEventEmitter: AdapterEventEmitter = AdapterEventEmitter(),
    val changePayload: (DomainModel, DomainModel) -> Any?
) : IDelegationAdapterExtended {


    class AdapterDiffUtil(private val changePayload: (DomainModel, DomainModel) -> Any?) :
        DiffUtil.Callback() {

        lateinit var oldList: List<DomainModel>
        lateinit var newList: List<DomainModel>


        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return changePayload(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private lateinit var recyclerView: RecyclerView

    fun onAttachRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        (adapter as RecyclerView.Adapter<*>).registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {

            //todo добавить все слушатели

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adapterEventEmitter.onNotifyAdapterItemInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                adapterEventEmitter.onNotifyAdapterItemRemoved(positionStart, itemCount)
            }

        })
    }

    val delegates: SparseArrayCompat<AdapterDelegate> = SparseArrayCompat()
    override val listItems: MutableList<Any> = mutableListOf()

    override fun notifyAdapterItemChanged(position: Int, payload: Any) {

    }

    override fun notifyAdapterItemChanged(position: Int) {

    }

    override fun notifyAdapterItemRangeChanged(
        positionStart: Int,
        itemCount: Int,
        payload: Any
    ) {

    }

    override fun notifyAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {

    }

    override fun notifyAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {

    }

    override fun notifyAdapterItemSetChanged() {

    }

    override fun notifyAdapterItemInserted(position: Int) {

    }

    override fun notifyAdapterItemRemoved(position: Int) {

    }

    override fun notifyAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {

    }

    override fun notifyAdapterItemMoved(fromPosition: Int, toPosition: Int) {

    }

    fun attachAdapter(adapter: IDelegationAdapterExtended) {
        if (this::adapter.isInitialized) throw IllegalStateException("Adapter has already attached")
        this.adapter = adapter
    }

    private val diffUtil: AdapterDiffUtil = AdapterDiffUtil(changePayload)
    private val extensions: MutableMap<Class<*>, AdapterDelegateExtension> = mutableMapOf()
    private val actionHistory: ActionHistory = ActionHistory()
    private lateinit var adapter: IDelegationAdapterExtended

    init {
        for (delegate in delegates) {
            addDelegate(delegate)
        }
    }

    fun addDelegate(delegate: AdapterDelegate) {
        var viewType = delegates.size()
        while (delegates[viewType] != null) {
            viewType++
        }
        delegates.put(viewType, delegate)
    }

    fun getItemViewType(items: MutableList<Any>, position: Int): Int {
        val delegatesCount = delegates.size()
        for (i in 0 until delegatesCount) {
            val delegate = delegates.valueAt(i)
            if (delegate.isForViewType(listItems, position)) {
                return delegates.keyAt(i)
            }
        }
        throw IllegalArgumentException("${listItems[position]}")
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val delegate = delegates[viewType] ?: throw IllegalArgumentException()
        val createdHolder = delegate.onCreateViewHolder(parent)
        adapterEventEmitter.onCreateViewHolderEvent(createdHolder)
        return createdHolder
    }

    fun onBindViewHolder(
        items: MutableList<Any>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val delegate = delegates[holder.itemViewType] ?: throw IllegalArgumentException()
        delegate.onBindViewHolder(holder, items, position)
        adapterEventEmitter.onBindViewHolderEvent(holder)
    }

    fun onBindViewHolder(
        items: List<Any>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val delegate = delegates[holder.itemViewType] ?: throw IllegalArgumentException()
        delegate.onBindViewHolder(holder, items, position, payloads)
    }

    fun <T : AdapterDelegateExtension> addFeature(
        delegateAdapterFeature: T,
        featureScope: (T) -> Unit
    ) {
        delegateAdapterFeature.onAttach(adapter)
        featureScope(delegateAdapterFeature)
    }

    fun <T : AdapterDelegateExtension> addExtensions(vararg extensions: T) {
        extensions.forEach {
            this.extensions[it::class.java] = it
            it.onAttach(adapter)
        }
    }

    fun addOnItemClickListener(onItemClickListener: (position: Int) -> Unit) {
        adapterEventEmitter.addOnItemClickObserver { onItemClickListener(it.bindingAdapterPosition) }
    }

    override fun <T : AdapterDelegateExtension> extension(clazz: Class<T>): T {
        val adapterDelegateExtension = extensions[clazz]
        return adapterDelegateExtension as T
    }

    override fun delegatesCount(): Int = delegates.size()

    override fun add(item: Any) {
        listItems.add(item)
        adapter.notifyAdapterItemInserted(count)
    }

    override fun add(items: List<Any>) {
        this.listItems.addAll(items)
        adapter.notifyAdapterItemRangeInserted(count, items.size)
    }

    override fun add(item: Any, position: Int) {
        listItems.add(position, item)
        adapter.notifyAdapterItemInserted(position)
//        actionHistory.add(
//            AddItemsAction(
//                adapter as RecyclerView.Adapter<*>,
//                items,
//                listOf(item)
//            )
//        )
    }

    override fun add(items: List<Any>, position: Int) {
        listItems.addAll(position, items)
        adapter.notifyAdapterItemRangeInserted(position, items.size)
//        actionHistory.add(AddItemsAction(adapter as RecyclerView.Adapter<*>, this.items, items))
    }

    override fun set(items: List<Any>) {
        listItems.clear()
        listItems.addAll(items)
        adapter.notifyAdapterItemSetChanged()
    }

    override fun replace(item: Any, position: Int) {
        listItems[position] = item
        adapter.notifyAdapterItemChanged(position)
//        actionHistory.add(
//            ReplaceItemsAction(
//                adapter as RecyclerView.Adapter<*>,
//                items,
//                listOf(item),
//                position
//            )
//        )
    }

    override fun replace(items: List<Any>, position: Int) {
        for (i in items.indices) {
            this.listItems.removeAt(position)
        }
        this.listItems.addAll(items)
        adapter.notifyAdapterItemRangeChanged(position, items.size)

//        actionHistory.add(
//            ReplaceItemsAction(
//                adapter as RecyclerView.Adapter<*>,
//                this.items,
//                items,
//                position
//            )
//        )
    }

    override fun submit(items: List<Any>) {
        val oldList = (this.listItems as List<DomainModel>).toList()
        this.listItems.clear()
        this.listItems.addAll(items)
        diffUtil.oldList = oldList
        diffUtil.newList = items as List<DomainModel>
        val diffResult: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(diffUtil)
        diffResult.dispatchUpdatesTo(adapter as RecyclerView.Adapter<*>)
    }

//    override fun remove(position: Int) {
//        listItems.removeAt(position)
//        adapter.notifyAdapterItemRemoved(position)
//    }

    override fun remove(positionStart: Int, count: Int) {
        for (i in 0 until count) {
            this.listItems.removeAt(positionStart)
        }
        adapter.notifyAdapterItemRangeRemoved(positionStart, count)
    }

    override fun undo() {
        actionHistory.undo()
    }
}

interface ObserverEventEmitter {

    fun addOnCreateObserver(onCreateObserver: (RecyclerView.ViewHolder) -> Unit)

    fun addOnBindObserver(onBindObserver: (RecyclerView.ViewHolder) -> Unit)

    fun addOnItemClickObserver(onItemClickObserver: (RecyclerView.ViewHolder) -> Unit)

    fun addOnItemLongClickObserver(onItemLongClickObserver: (RecyclerView.ViewHolder) -> Unit)


    fun addOnNotifyItemRemovedObserver(onNotifyItemRemovedObserver: (position: Int, itemCount: Int) -> Unit)

    fun addOnNotifyItemInsertedObserver(onNotifyItemRemovedObserver: (position: Int, itemCount: Int) -> Unit)
}

interface DelegateEventEmitter {

    fun onNotifyAdapterItemChanged(positionStart: Int, itemCount: Int, payload: Any)

    fun onNotifyAdapterItemChanged(position: Int)

    fun onNotifyAdapterItemChanged(positionStart: Int, itemCount: Int)

    fun onNotifyAdapterItemRangeInserted(positionStart: Int, itemCount: Int)

    fun onNotifyAdapterItemInserted(positionStart: Int, itemCount: Int)

    fun onNotifyAdapterItemRemoved(positionStart: Int, itemCount: Int)

    fun onNotifyAdapterItemMoved(positionStart: Int, itemCount: Int)
}

interface IEVentEmitter : ObserverEventEmitter, DelegateEventEmitter
