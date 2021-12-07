package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.google.gson.annotations.SerializedName


data class ListItem(
    @SerializedName("id")
    override var uuid: String = "",
    var title: String = "",
    var icon: EitherResource = emptyIcon,
    var color: EitherResource = emptyColor,
    var content: Any? = null,
    val enable: Boolean = true,
    var type: Int = 0
) : DomainModel() {

    companion object {
        val emptyIcon = EitherResource.Id(0)

        val emptyColor = EitherResource.Id(0)
    }

    fun hasIcon(): Boolean = icon != emptyIcon
}

sealed class EitherResource {

    data class Id(val a: Int) : EitherResource()

    data class String(val b: kotlin.String) : EitherResource()

    val isString get() = this is String

    val isId get() = this is Id

    fun fold(fnL: (Int) -> Any, fnR: (kotlin.String) -> Any): Any =
        when (this) {
            is Id -> fnL(a)
            is String -> fnR(b)
        }
}

fun EitherResource.onString(fn: (success: String) -> Unit): EitherResource =
    this.apply { if (this is EitherResource.String) fn(b) }

fun EitherResource.onId(fn: (failure: Int) -> Unit): EitherResource =
    this.apply { if (this is EitherResource.Id) fn(a) }