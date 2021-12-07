package com.denchic45.kts.utils

import android.content.Context
import android.content.res.Configuration

object AppConfig {

    var density = 1f
    var fontDensity = 1f

    fun onConfigChanged(context: Context, newConfiguration: Configuration?) {
        val configuration = newConfiguration ?: context.resources.configuration

        density = context.resources.displayMetrics.density
        fontDensity = context.resources.displayMetrics.scaledDensity
    }
}