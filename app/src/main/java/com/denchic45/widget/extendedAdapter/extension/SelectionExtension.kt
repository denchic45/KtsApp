package com.denchic45.widget.extendedAdapter.extension

import com.denchic45.widget.extendedAdapter.AdapterDelegateExtension
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.IDelegationAdapterExtended
import com.denchic45.widget.extendedAdapter.extension.SelectionExtension.Companion.PAYLOAD_FINISH_SELECTION
import com.denchic45.widget.extendedAdapter.extension.SelectionExtension.Companion.PAYLOAD_ITEM_DESELECT
import com.denchic45.widget.extendedAdapter.extension.SelectionExtension.Companion.PAYLOAD_ITEM_SELECT
import com.denchic45.widget.extendedAdapter.extension.SelectionExtension.Companion.PAYLOAD_START_SELECTION
import com.denchic45.widget.extendedAdapter.extension.SelectionExtension.Companion.SELECTION_ULTIMATED
import java.util.*


fun DelegationAdapterDsl.ExtensionsBuilder.selection(block: SelectionExtensionBuilder.() -> Unit) {
    return add(SelectionExtensionBuilder().apply(block).build())
}

open class SelectionExtension(
    private val maxSelect: Int,
    private val autoSelect: Boolean,
    private val autoDeselect: Boolean,
    private val startSelectOn: SELECT,
    private val payloadOnStart: Any,
    private val payloadOnSelect: Any,
    private val payloadOnDeselect: Any,
    private val payloadOnFinish: Any
) : AdapterDelegateExtension {

    companion object {
        const val PAYLOAD_START_SELECTION = "START_SELECTION"
        const val PAYLOAD_ITEM_SELECT = "ITEM_SELECT"
        const val PAYLOAD_ITEM_DESELECT = "ITEM_DESELECT"
        const val PAYLOAD_FINISH_SELECTION = "FINISH_FINISH_SELECTION"
        const val SELECTION_ULTIMATED = -1
    }

    var isStarted: Boolean = false
        protected set
    private val _selectionIndexes: MutableList<Int> = LinkedList()
    val selectionIndexes: List<Int>
        get() = _selectionIndexes
    lateinit var adapter: IDelegationAdapterExtended


    override fun onAttach(adapterExtended: IDelegationAdapterExtended) {
        this.adapter = adapterExtended
        adapterExtended.adapterEventEmitter.addOnItemClickObserver {
            if (startSelectOn == SELECT.ON_CLICK && !isStarted) {
                select(it.bindingAdapterPosition)
            } else handleSelection(it.bindingAdapterPosition)

        }
        adapterExtended.adapterEventEmitter.addOnItemLongClickObserver {
            if (startSelectOn == SELECT.ON_LONG_CLICK && !isStarted) {
                select(it.bindingAdapterPosition)
            }
//            else handleSelection(it.bindingAdapterPosition)
        }
        adapterExtended.adapterEventEmitter.addOnNotifyItemRemovedObserver { positionStart, itemCount ->
            _selectionIndexes.removeIf {
                it in positionStart until itemCount + positionStart
            }
            for ((index, value) in _selectionIndexes.withIndex()) {
                if (value > positionStart)
                    _selectionIndexes[index] = value - itemCount
            }
//            _selectionIndexes.remove(positionStart)
//            deselect(_selectionIndexes[0])
        }
        adapterExtended.adapterEventEmitter.addOnNotifyItemInsertedObserver { positionStart, itemCount ->
            for ((index, value) in _selectionIndexes.withIndex()) {
                if (value >= positionStart)
                    _selectionIndexes[index] = value + itemCount
            }
//            _selectionIndexes.remove(positionStart)
//            deselect(_selectionIndexes[0])
        }
    }

    private fun handleSelection(position: Int) {
        if (autoSelect && !_selectionIndexes.contains(position))
            select(position)
        else if (autoDeselect)
            deselect(position)
    }

    fun select(position: Int, count: Int = 1) {
        if (adapter.count < position + count)
            return
        startSelectIfNecessary()
        for (i in 0 until count) {
            _selectionIndexes.add(i + position)
            adapter.notifyAdapterItemChanged(i + position, payloadOnSelect)
            if (maxSelect < _selectionIndexes.size) {
                deselect(_selectionIndexes[0])
            }
        }
    }

    fun selectAll() {
        select(0, adapter.count)
    }

    fun deselect(position: Int, count: Int = 1) {
        for (i in 0 until count) {
            _selectionIndexes.remove(i + position)
            adapter.notifyAdapterItemChanged(i + position, payloadOnDeselect)
        }
        finishSelectIfNecessary()
    }

    fun deselectAll() {
        deselect(0, adapter.count)
    }

    private fun startSelectIfNecessary() {
        if (!isStarted) {
            adapter.notifyAdapterItemRangeChanged(0, adapter.count, payloadOnStart)
            isStarted = true
        }
    }

    private fun finishSelectIfNecessary() {
        if (isStarted && _selectionIndexes.isEmpty()) {
            adapter.notifyAdapterItemRangeChanged(0, adapter.count, payloadOnFinish)
            isStarted = false
        }
    }

    fun isSelected(position: Int): Boolean = _selectionIndexes.contains(position)


    enum class SELECT { ON_CLICK, ON_LONG_CLICK, NOTHING }
}

class SelectionExtensionBuilder {

    var startSelectOn: SelectionExtension.SELECT = SelectionExtension.SELECT.ON_LONG_CLICK
    var maxSelect = SELECTION_ULTIMATED
    var autoSelect = true
    var autoDeselect = true
    var payloadOnStart: Any = PAYLOAD_START_SELECTION
    var payloadOnSelect: Any = PAYLOAD_ITEM_SELECT
    var payloadOnDeselect: Any = PAYLOAD_ITEM_DESELECT
    var payloadOnFinish: Any = PAYLOAD_FINISH_SELECTION

    fun build(): SelectionExtension {
        return SelectionExtension(
            maxSelect,
            autoSelect,
            autoDeselect,
            startSelectOn,
            payloadOnStart,
            payloadOnSelect,
            payloadOnDeselect,
            payloadOnFinish
        )
    }
}