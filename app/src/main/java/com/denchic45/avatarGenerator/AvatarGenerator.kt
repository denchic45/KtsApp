package com.denchic45.avatarGenerator

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.sqrt

class AvatarGenerator(private val context: Context) {
    private var rect: Rect? = null
    private var canvas: Canvas? = null
    private var paint: Paint? = null
    private val randomColor = RandomColor()

    @Contract("_, _, _, _ -> new")
    private fun generateBitmapDrawable(
        name: String?,
        size: Int,
        color: Int,
        font: Typeface?
    ): BitmapDrawable {
        var name = name!!
        var size = size
        size = if (size == 0) DEF_AVATAR_SIZE else size
        rect = Rect(0, 0, size, size)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (color == 0) paint!!.color = randomColor.randomColor else paint!!.color = color
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas!!.drawRect(rect!!, paint!!)
        name = formatText(name)
        val textPaint = createTextPainter(font, canvas!!)
        paint!!.getTextBounds(name, 0, name.length, rect)
        val x = canvas!!.width / 2
        val y = (canvas!!.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
        drawTextCentred(name, size.toFloat())
        canvas!!.drawText(name, x.toFloat(), y.toFloat(), textPaint)
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun formatText(name: String): String {
        return name.substring(0, 1).uppercase(Locale.getDefault())
    }

    private fun createTextPainter(font: Typeface?, canvas: Canvas): TextPaint {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.isAntiAlias = true
        val relation = sqrt((canvas.width * canvas.height).toDouble()) / 250
        textPaint.textSize = (126 * relation).toFloat()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        if (font != null) textPaint.typeface = font
        return textPaint
    }

    fun drawTextCentred(text: String, size: Float) {
        paint!!.getTextBounds(text, 0, text.length, rect)
        canvas!!.drawText(text, size - rect!!.exactCenterX(), size - rect!!.exactCenterY(), paint!!)
    }

    class Builder(val context: Context) {
        var name: String? = null
            private set
        var color = 0
            private set
        var font: Typeface? = null
            private set
        var size = 0
            private set

        fun initFrom(initializer: AvatarBuilderInitializer): Builder {
            val builder = initializer.initBuilder(this)
            color = builder.color
            font = builder.font
            size = builder.size
            return this
        }

        fun name(name: String?): Builder {
            this.name = name
            return this
        }

        fun color(color: Int): Builder {
            this.color = color
            return this
        }

        fun color(color: String?): Builder {
            this.color = Color.parseColor(color)
            return this
        }

        fun size(size: Int): Builder {
            this.size = size
            return this
        }

        fun font(font: Typeface?): Builder {
            this.font = font
            return this
        }

        fun generateBitmapDrawable(): BitmapDrawable {
            return AvatarGenerator(context).generateBitmapDrawable(name, size, color, font)
        }

        fun generateBytes(): ByteArray {
            val avatar = AvatarGenerator(context).generateBitmapDrawable(name, size, color, font)
            val stream = ByteArrayOutputStream()
            avatar.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
    }

    companion object {
        const val DEF_AVATAR_SIZE = 192
    }
}