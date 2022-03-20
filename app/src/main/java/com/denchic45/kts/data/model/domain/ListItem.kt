package com.denchic45.kts.data.model.domain

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
) : DomainModel() {

    companion object {
        val emptyIcon = EitherMessage.Id(0)

        val emptyColor = EitherMessage.Id(0)
    }

    fun hasIcon(): Boolean = icon != emptyIcon
}

sealed class EitherMessage {

    data class Id(val value: Int) : EitherMessage()

    data class String(val value: kotlin.String) : EitherMessage()

    val isString get() = this is String

    val isId get() = this is Id

    fun fold(fnL: (Int) -> Any, fnR: (kotlin.String) -> Any): Any =
        when (this) {
            is Id -> fnL(value)
            is String -> fnR(value)
        }
}

fun EitherMessage.onString(fn: (success: String) -> Unit): EitherMessage =
    this.apply { if (this is EitherMessage.String) fn(value) }

fun EitherMessage.onId(fn: (failure: Int) -> Unit): EitherMessage =
    this.apply { if (this is EitherMessage.Id) fn(value) }