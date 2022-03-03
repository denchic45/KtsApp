package com.denchic45.kts.ui.adapter

interface OnItemMoveListener {
    fun onMove(oldPosition: Int, targetPosition: Int, dayOfWeek: Int)
}