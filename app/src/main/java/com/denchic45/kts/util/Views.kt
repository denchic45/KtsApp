package com.denchic45.kts.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.annotation.ColorRes
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.denchic45.kts.ui.course.submission.windowHeight
import com.denchic45.kts.ui.updateView.getActivity

fun View.getLayoutInflater(attachToParent: Boolean = false): LayoutInflater =
    LayoutInflater.from(context)


inline fun <T : ViewBinding> ViewGroup.viewBinding(
    crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
    attachToParent: Boolean = false
) = bindingInflater.invoke(LayoutInflater.from(this.context), this, attachToParent)

inline var View.visibleOrGone: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

inline var View.visibleOrInvisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

fun Activity.windowWidth(): Int {
    val view = window.decorView
    val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view)
        .getInsets(WindowInsetsCompat.Type.systemBars())
    return resources.displayMetrics.widthPixels - insets.left - insets.right
}

fun ViewFlipper.animateHeight(
//    activity: Activity,
//    windowHeight: Int,
//    windowWidth: Int = activity.widthWidth(),
) {

    val windowWidth = getActivity()!!.windowWidth()
    val windowHeight = getActivity()!!.windowHeight()

//        inline val androidx.fragment.app.Fragment.windowHeight: Int
//        get() {
//            val view = requireActivity().window.decorView
//            return resources.displayMetrics.heightPixels
//        }


    fun preMeasureViewHeight(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(windowWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(windowHeight, View.MeasureSpec.AT_MOST)
        )
        return measuredHeight
    }

    fun changeBottomSheetHeight(`val`: Int) {
        val layoutParams = layoutParams
        layoutParams.height = `val`
        this.layoutParams = layoutParams
    }

    val newHeight = preMeasureViewHeight()
    val anim = ValueAnimator.ofInt(height, newHeight)

    anim.addUpdateListener { valueAnimator ->
        val `val` = valueAnimator.animatedValue as Int
        changeBottomSheetHeight(`val`)
    }
    anim.interpolator = DecelerateInterpolator()
    anim.duration = 300
    anim.start()
    anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            changeBottomSheetHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    })
}

fun ImageView.paintColor(@ColorRes colorRes: Int) {
    val paint = Paint()
    val colorFilter = PorterDuffColorFilter(context.colors(colorRes), PorterDuff.Mode.SRC_ATOP)
    paint.colorFilter = colorFilter
    setLayerPaint(paint)

}