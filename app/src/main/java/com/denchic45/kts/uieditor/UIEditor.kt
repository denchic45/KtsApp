package com.denchic45.kts.uieditor

import com.denchic45.kts.data.model.DomainModel


class UIEditor<T : DomainModel> @JvmOverloads constructor(
    val isNew: Boolean = true,
    val createItem: () -> T = { throw Throwable("Nothing create item lambda!") }
) {

    val item: T
        get() = createItem()

    var oldItem: T? = null

    init {
//        oldItem = createInstance()
    }

//    fun createInstance(): T {
//        return createSupplier.get()
//    }

//    fun updateItem(reduce: T.() -> T) {
//        val newItem = item.reduce()
//        item = newItem
//    }

    fun hasBeenChanged(): Boolean {
        return oldItem != createItem()
    }
}