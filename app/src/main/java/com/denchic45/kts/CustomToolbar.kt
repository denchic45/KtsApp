package com.denchic45.kts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.text.TextUtils
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat

class CustomToolbar : Toolbar {
    var sizeText =
        (resources.getDimension(R.dimen.large_text) / resources.displayMetrics.density).toInt()
    private var textView: TextView? = null
    private var screenWidth = 0
    private var centerTitle = false

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setBackgroundColor(Color.TRANSPARENT)
        screenWidth = screenSize.x
        textView = TextView(context)
        textView!!.textSize = sizeText.toFloat()
        val type = ResourcesCompat.getFont(context, R.font.gilroy_medium)
        textView!!.typeface = type
        textView!!.isSingleLine = true
        textView!!.ellipsize = TextUtils.TruncateAt.END
        addView(textView)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (centerTitle) {
            val location = IntArray(2)
            textView!!.getLocationOnScreen(location)
            textView!!.translationX =
                textView!!.translationX + (-location[0] + screenWidth / 2 - textView!!.width / 2)
        } else textView!!.translationX = 0f
    }

    fun pxToDp(dpValue: Float): Int {
        val dp = resources.displayMetrics.density
        return (dpValue * dp).toInt() // margin in pixels
    }

    override fun setTitle(title: CharSequence) {
        textView!!.text = title
        requestLayout()
    }

    override fun setTitle(titleRes: Int) {
        textView!!.setText(titleRes)
        requestLayout()
    }

    fun setTitleCentered(centered: Boolean) {
        centerTitle = centered
        requestLayout()
    }

    private val screenSize: Point
        get() {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val screenSize = Point()
            display.getSize(screenSize)
            return screenSize
        }
}