package com.denchic45.widget.extendedAdapter.extension

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.widget.extendedAdapter.AdapterDelegateExtension
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.IDelegationAdapterExtended

fun DelegationAdapterDsl.ExtensionsBuilder.click(block: ClickExtensionBuilder.() -> Unit) {
    return add(ClickExtensionBuilder().apply(block).build())
}

class ClickExtensionBuilder {

    var onClick: (position: Int) -> Unit = {}
    var predicate: (RecyclerView.ViewHolder) -> Boolean = { true }
    var view: (RecyclerView.ViewHolder) -> View = { viewHolder -> viewHolder.itemView }

    fun build(): ClickExtension {
        return ClickExtension(
            onClick,
            predicate,
            view,
        )
    }
}

class ClickExtension(
    private val onClick: (position: Int) -> Unit,
    private val predicate: (RecyclerView.ViewHolder) -> Boolean = { true },
    private val view: (RecyclerView.ViewHolder) -> View = { viewHolder -> viewHolder.itemView }
) : AdapterDelegateExtension {

    override fun onAttach(
        adapterExtended: IDelegationAdapterExtended
    ) {
        adapterExtended.adapterEventEmitter.addOnCreateObserver { viewHolder ->
            if (predicate(viewHolder)) {
                view(viewHolder).setOnClickListener {
                    onClick(viewHolder.absoluteAdapterPosition)
                }
            }
        }
    }
}