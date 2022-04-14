package com.denchic45.kts.data.model.domain

import android.content.Context
import com.denchic45.kts.data.model.DomainModel
import com.google.gson.annotations.SerializedName


data class ListItem(
    @SerializedName("_id")
    override var id: String = "",
    var title: String = "",
    var icon: EitherMessage = emptyIcon,
    var color: EitherMessage = emptyColor,
    var content: Any? = null,
    val enable: Boolean = true,
    var type: Int = 0
) : DomainModel {

    companion object {
        val emptyIcon = EitherMessage.Id(0)

        val emptyColor = EitherMessage.Id(0)
    }

    fun hasIcon(): Boolean = icon != emptyIcon
}

sealed class EitherMessage {

    data class Stroke(val value: String) : EitherMessage()

    data class Id(val value: Int) : EitherMessage()

    data class FormattedString(val value: Int, val formatArgs:Any?): EitherMessage() {
        fun get(context: Context): String {
            return context.resources.getString(value, formatArgs)
        }
    }

    data class FormattedQuantityString(val value: Int, val quantity: Int, val formatArgs:Any?): EitherMessage()

    val isString get() = this is Stroke

    val isId get() = this is Id

    fun fold(fnL: (Int) -> Any, fnR: (String) -> Any): Any =
        when (this) {
            is Id -> fnL(value)
            is Stroke -> fnR(value)
            else -> throw IllegalStateException()
        }
}

fun EitherMessage.onString(fn: (success: String) -> Unit): EitherMessage =
    this.apply { if (this is EitherMessage.Stroke) fn(value) }

fun EitherMessage.onId(fn: (failure: Int) -> Unit): EitherMessage =
    this.apply { if (this is EitherMessage.Id) fn(value) }