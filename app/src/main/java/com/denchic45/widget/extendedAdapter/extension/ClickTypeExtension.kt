package com.denchic45.widget.extendedAdapter.extension

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl

inline fun <reified VH : RecyclerView.ViewHolder> DelegationAdapterDsl.ExtensionsBuilder.click(
    block: ClickTypeExtensionBuilder<VH>.() -> Unit
) {
    return add(
        ClickTypeExtensionBuilder(VH::class.java).apply(block).build()
    )
}

class ClickTypeExtensionBuilder<VH : RecyclerView.ViewHolder>(
    private val vhClassType: Class<VH>,
) {
    var onClick: (position: Int) -> Unit = {}
    var onLongClick: (position: Int) -> Boolean = { false }
    var predicate: (VH) -> Boolean = { true }
    var view: (VH) -> View = { viewHolder -> viewHolder.itemView }

    fun build(): ClickTypeExtension<VH> {
        return ClickTypeExtension(
            vhClassType,
            predicate,
            view,
            onClick,
            onLongClick
        )
    }
}

class ClickTypeExtension<VH : RecyclerView.ViewHolder>(
    vhClassType: Class<VH>,
    predicate: (VH) -> Boolean = { true },
    view: (VH) -> View,
    private val onClick: (position: Int) -> Unit,
    private val onLongClick: (position: Int) -> Boolean,
) : BaseViewEventExtension<VH>(vhClassType, onClick, predicate, view) {
    override fun onViewDone(viewHolder: VH) {
        view(viewHolder).setOnClickListener {
            onClick(viewHolder.absoluteAdapterPosition)
        }

        view(viewHolder).setOnLongClickListener {
            onLongClick(viewHolder.absoluteAdapterPosition)
        }
    }


}