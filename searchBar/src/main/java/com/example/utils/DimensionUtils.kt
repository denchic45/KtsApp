package com.example.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object DimensionUtils {
    fun pxToDp(px: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}