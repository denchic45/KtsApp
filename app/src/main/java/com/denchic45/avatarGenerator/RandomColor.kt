package com.denchic45.avatarGenerator

import android.graphics.Color
import java.util.*

class RandomColor {
    private val defColors = listOf(
        Color.parseColor("#303F9F"),
        Color.parseColor("#EF6C00"),
        Color.parseColor("#00bcd4"),
        Color.parseColor("#455A64"),
        Color.parseColor("#e53935"),
        Color.parseColor("#4caf50"),
        Color.parseColor("#004C3F"),
        Color.parseColor("#7E57C2"),
        Color.parseColor("#689f39"),
    )

    val randomColor: Int
        get() = defColors[Random().nextInt(defColors.size)]
}