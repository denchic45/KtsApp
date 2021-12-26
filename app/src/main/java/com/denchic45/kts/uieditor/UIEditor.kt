package com.denchic45.kts.uieditor

import android.util.Log
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
        Log.d("lol", "hasBeenChanged course: ${oldItem} \nand ${createItem()}")
        return oldItem != createItem()
    }
}