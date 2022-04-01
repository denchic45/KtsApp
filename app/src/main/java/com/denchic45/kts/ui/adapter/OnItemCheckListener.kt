package com.denchic45.kts.ui.adapter

fun interface OnItemCheckListener {
    fun onItemCheck(position: Int, isChecked: Boolean)
}