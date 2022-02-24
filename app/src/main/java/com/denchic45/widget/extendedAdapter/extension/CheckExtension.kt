package com.denchic45.widget.extendedAdapter.extension

import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.denchic45.widget.extendedAdapter.AdapterDelegateExtension
import com.denchic45.widget.extendedAdapter.DelegationAdapterDsl
import com.denchic45.widget.extendedAdapter.IDelegationAdapterExtended

inline fun <reified VH : RecyclerView.ViewHolder> DelegationAdapterDsl.ExtensionsBuilder.check(
    noinline view: (VH) -> CompoundButton,
    noinline onCheck: (position: Int, checked: Boolean) -> Unit = { _, _ -> }
) {
    return add(
        CheckExtension(
            VH::class.java,
            view,
            onCheck
        )
    )
}

class CheckExtension<VH : RecyclerView.ViewHolder>(
    private val vhClassType: Class<VH>,
    private val view: (VH) -> CompoundButton,
    private val onCheck: (position: Int, checked: Boolean) -> Unit,
) : AdapterDelegateExtension {

    private fun isNotCorrectType(viewHolder: RecyclerView.ViewHolder): Boolean {
        return vhClassType != viewHolder::class.java
    }

    override fun onAttach(adapterExtended: IDelegationAdapterExtended) {
        adapterExtended.adapterEventEmitter.addOnCreateObserver { viewHolder ->
            if (adapterExtended.delegatesCount() != 1 && isNotCorrectType(viewHolder))
                return@addOnCreateObserver
            view(viewHolder as VH).setOnCheckedChangeListener { _, checked ->
                onCheck(
                    viewHolder.absoluteAdapterPosition,
                    checked
                )
            }
        }
    }

}