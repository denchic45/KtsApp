package com.denchic45.kts.util

object Orders {
    fun getBetweenOrders(prevOrder: Long, nextOrder: Long): Long {
        return (prevOrder + nextOrder) / 2
    }
}