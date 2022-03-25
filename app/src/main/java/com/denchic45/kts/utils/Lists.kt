package com.denchic45.kts.utils

fun <T> List<T>.swap(index1: Int, index2: Int): List<T> {
    return toMutableList().apply {
        val tmp = this[index1] // 'this' corresponds to the list
        this[index1] = this[index2]
        this[index2] = tmp
    }
}