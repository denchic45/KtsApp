package com.denchic45.kts.utils

import android.text.InputFilter
import android.text.Spanned

class ValueFilter(
    private var min: Int,
    private var max: Int
) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {

        try {
            val input = if (dend != dest.count()) {
                (dest.substring(0, dstart) + source + dest.substring(dstart, dest.count())).toInt()
            } else {
                (dest.toString() + source.toString()).toInt()
            }
            if (isInRange(min, max, input)) return source
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        }
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}