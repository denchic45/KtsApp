package com.denchic45.widget.extendedAdapter

import com.denchic45.widget.extendedAdapter.action.AdapterNotifier

interface IDelegationAdapterExtended : IAdapterManager, AdapterNotifier {
    fun <T : AdapterDelegateExtension> extension(clazz: Class<T>): T

    fun delegatesCount(): Int

//    val items: MutableList<Any>
//
//    val count : Int
//        get() = items.size

//    fun attachAdapter(adapter: IDelegationAdapterExtended)
}