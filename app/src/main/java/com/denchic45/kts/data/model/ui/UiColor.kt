package com.denchic45.kts.data.model.ui

sealed class UiColor {

    data class ColorName(val value: String) : UiColor()

    data class ColorId(val value: Int) : UiColor()
}

fun UiColor.onUrl(fn: (success: String) -> Unit): UiColor =
    this.apply { if (this is UiColor.ColorName) fn(value) }

fun UiColor.onId(fn: (failure: Int) -> Unit): UiColor =
    this.apply { if (this is UiColor.ColorId) fn(value) }