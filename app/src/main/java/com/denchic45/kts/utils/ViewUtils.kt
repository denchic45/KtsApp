package com.denchic45.kts.utils

import android.content.Context
import android.graphics.Paint
import android.view.ViewGroup
import android.widget.FrameLayout
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.ListAdapter
import androidx.core.content.ContextCompat

object ViewUtils {
    fun getParent(view: View): ViewGroup {
        return view.parent as ViewGroup
    }

    fun measureAdapter(listAdapter: ListAdapter, context: Context?): Int {
        val mMeasureParent: ViewGroup = FrameLayout(context!!)
        var longestWidth = listAdapter.getView(0, null, mMeasureParent).measuredWidth
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, mMeasureParent)
            listItem.measure(0, 0)
            val width = listItem.measuredWidth
            if (width > longestWidth) {
                longestWidth = width
            }
        }
        return longestWidth
    }

    fun paintImageView(iv: ImageView, color: Int, context: Context?) {
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context!!, color),
            PorterDuff.Mode.SRC_ATOP
        )
        iv.setLayerPaint(paint)
    }
}