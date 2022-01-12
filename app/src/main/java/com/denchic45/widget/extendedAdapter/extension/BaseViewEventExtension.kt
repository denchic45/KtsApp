package com.denchic45.widget.extendedAdapter.extension

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.widget.extendedAdapter.AdapterDelegateExtension
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.IDelegationAdapterExtended



abstract class BaseViewEventExtension<VH : RecyclerView.ViewHolder>(
    private val vhClassType: Class<VH>,
    private val onClick: (position: Int) -> Unit,
    private val predicate: (VH) -> Boolean = { true },
    val view: (VH) -> View
) : AdapterDelegateExtension {

    private fun isNotCorrectType(viewHolder: RecyclerView.ViewHolder): Boolean {
        return vhClassType != viewHolder::class.java
    }

    override fun onAttach(
        adapterExtended: IDelegationAdapterExtended
    ) {
        adapterExtended.adapterEventEmitter.addOnCreateObserver { viewHolder ->
            if (adapterExtended.delegatesCount() != 1 && isNotCorrectType(viewHolder))
                return@addOnCreateObserver
            if (predicate(viewHolder as VH)) {
                onViewDone(viewHolder)
            }
        }
    }

    abstract fun onViewDone(viewHolder: VH)
}