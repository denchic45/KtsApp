package com.denchic45.kts.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Movie
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat.*

fun Context.animations(id: Int) = resources.getAnimation(id)

fun Context.booleans(id: Int) = resources.getBoolean(id)

fun Context.colors(@ColorRes id: Int) = getColor(resources, id, theme)

fun Context.colorStateLists(id: Int) = getColorStateList(resources, id, theme)!!

fun Context.dimens(id: Int) = resources.getDimension(id)

fun Context.dimensInt(id: Int) = resources.getDimensionPixelSize(id)

fun Context.dimensOffset(id: Int) = resources.getDimensionPixelOffset(id)

fun Context.drawables(id: Int) = getDrawable(resources, id, theme)!!

fun Context.scaledDrawable(id: Int) = ScaledDrawable(resources, id, theme)

fun Context.fonts(id: Int) = getFont(this, id)!!

fun Context.intArrays(id: Int) = resources.getIntArray(id)

fun Context.ints(id: Int) = resources.getInteger(id)

fun Context.layouts(id: Int) = resources.getLayout(id)

fun Context.movies(id: Int): Movie = resources.getMovie(id)

fun Context.formattedStrings(id: Int) = FormattedString(resources, id)

fun Context.resourceInfos(id: Int) = ResourceInfo(resources, id)

fun Context.strings(id: Int) = getString(id)

fun Context.stringArrays(id: Int): Array<String> = resources.getStringArray(id)

fun Context.texts(id: Int) = getText(id)

fun Context.textArrays(id: Int): Array<CharSequence> = resources.getTextArray(id)

fun Context.xmls(id: Int) = resources.getXml(id)

fun Context.typedArrays(id: Int) = resources.obtainTypedArray(id)

fun Context.rawResources(id: Int) = resources.openRawResource(id)


class FormattedString(
    private val resources: Resources,
    private val resId: Int
) {
    operator fun invoke(vararg values: Any): String =
        resources.getString(resId, *values)

    operator fun invoke(quantity: Int): String =
        resources.getQuantityString(resId, quantity)

    operator fun invoke(quantity: Int, vararg values: Any): String =
        resources.getQuantityString(resId, quantity, *values)
}

data class ResourceInfo(
    private val resources: Resources,
    private val resId: Int,
    val entryName: String = resources.getResourceEntryName(resId),
    val name: String = resources.getResourceName(resId),
    val packageName: String = resources.getResourcePackageName(resId),
    val typeName: String = resources.getResourceTypeName(resId)
)

class ScaledDrawable(
    private val resources: Resources,
    private val resId: Int,
    private val theme: Resources.Theme
) {
    operator fun invoke(density: Int): Drawable =
        getDrawableForDensity(resources, resId, density, theme)!!
}
