package com.denchic45.kts.util

object Orders {
    fun getBetweenOrders(prevOrder: Int, nextOrder: Int): Int {
        return (prevOrder + nextOrder) / 2
    }
}