package com.denchic45.widget.extendedAdapter.extension

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.ui.adapter.BaseViewHolder
import com.denchic45.widget.extendedAdapter.AdapterDelegateExtension
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.IDelegationAdapterExtended
import java.lang.reflect.Type

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
    var predicate: (VH) -> Boolean = { true }
    var view: (VH) -> View = { viewHolder -> viewHolder.itemView }

    fun build(): ClickTypeExtension<VH> {

        return ClickTypeExtension(
            vhClassType,
            onClick,
            predicate,
            view,
        )
    }
}

class ClickTypeExtension<VH : RecyclerView.ViewHolder>(
    private val vhClassType: Class<VH>,
    private val onClick: (position: Int) -> Unit,
    private val predicate: (VH) -> Boolean = { true },
    val view: (VH) -> View
) :
    AdapterDelegateExtension {

    private fun isNotCorrectType(viewHolder: RecyclerView.ViewHolder): Boolean {
        Log.d("lol", "isNotCorrectType: $vhClassType")
        Log.d("lol", "isNotCorrectType: ${viewHolder::class.java}")
        return vhClassType != viewHolder::class.java
    }

    override fun onAttach(
        adapterExtended: IDelegationAdapterExtended
    ) {
        adapterExtended.adapterEventEmitter.addOnCreateObserver { viewHolder ->
            if (isNotCorrectType(viewHolder))
                return@addOnCreateObserver
            if (predicate(viewHolder as VH)) {
                view(viewHolder).setOnClickListener {
                    onClick(viewHolder.absoluteAdapterPosition)
                }
            }
        }
    }
}