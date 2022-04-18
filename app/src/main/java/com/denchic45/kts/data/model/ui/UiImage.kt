package com.denchic45.kts.data.model.ui

sealed class UiImage {

    data class Url(val value: String) : UiImage()

    data class IdImage(val value: Int) : UiImage()
}

fun UiImage.onUrl(fn: (success: String) -> Unit): UiImage =
    this.apply { if (this is UiImage.Url) fn(value) }

fun UiImage.onId(fn: (failure: Int) -> Unit): UiImage =
    this.apply { if (this is UiImage.IdImage) fn(value) }