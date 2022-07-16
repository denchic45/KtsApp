package com.denchic45.kts.util

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object Dimensions {
    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}

 fun Context.dpToPx(dp: Int) = Dimensions.dpToPx(dp, this)