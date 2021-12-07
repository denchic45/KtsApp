package com.denchic45.widget.extendedAdapter.action

interface AdapterAction {

    fun add(item: Any)

    fun add(items: List<Any>)

    fun set(items: List<Any>)

    fun add(item: Any, position: Int)

    fun add(items: List<Any>, position: Int)

    fun replace(item: Any, position: Int)

    fun replace(items: List<Any>, position: Int)

    fun submit(items: List<Any>)

//    fun remove(position: Int)

    fun remove(positionStart:Int, count: Int = 1)

    fun undo()
}