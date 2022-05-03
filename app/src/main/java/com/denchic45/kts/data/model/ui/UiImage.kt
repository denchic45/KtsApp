package com.denchic45.kts.data.model.ui

import android.graphics.drawable.Drawable

sealed class UiImage {

    data class Url(val value: String) : UiImage()

    data class IdImage(val value: Int) : UiImage()

    data class DrawableImage(val value: Drawable) : UiImage()
}

fun UiImage.onUrl(fn: (value: String) -> Unit): UiImage =
    this.apply { if (this is UiImage.Url) fn(value) }

fun UiImage.onId(fn: (value: Int) -> Unit): UiImage =
    this.apply { if (this is UiImage.IdImage) fn(value) }

fun UiImage.onDrawable(fn: (value: Drawable) -> Unit): UiImage =
    this.apply { if (this is UiImage.DrawableImage) fn(value) }