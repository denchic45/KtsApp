package com.denchic45.kts.data.model.ui

import android.content.Context

sealed class UiText {

    abstract fun get(context: Context): String

    data class StringText(val value: String) : UiText() {
        override fun get(context: Context): String = value
    }

    data class IdText(val value: Int) : UiText() {
        override fun get(context: Context): String {
            return context.getString(value)
        }
    }

    data class FormattedText(val value: Int, val formatArgs: Any?) : UiText() {
        override fun get(context: Context): String {
            return context.resources.getString(value, formatArgs)
        }
    }

    data class FormattedQuantityText(val value: Int, val quantity: Int, val formatArgs: Any?) :
        UiText() {
        override fun get(context: Context): String {
            return context.resources.getQuantityString(value, quantity, formatArgs)
        }
    }

    fun fold(fnL: (Int) -> Any, fnR: (String) -> Any): Any =
        when (this) {
            is IdText -> fnL(value)
            is StringText -> fnR(value)
            else -> throw IllegalStateException()
        }
}

fun UiText.onString(fn: (success: String) -> Unit): UiText =
    this.apply { if (this is UiText.StringText) fn(value) }

fun UiText.onId(fn: (failure: Int) -> Unit): UiText =
    this.apply { if (this is UiText.IdText) fn(value) }