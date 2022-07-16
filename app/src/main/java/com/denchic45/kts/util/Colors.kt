package com.denchic45.kts.util

import com.denchic45.kts.R

//val colors: List<ListItem> = listOf(
//    ListItem(id = "", title = "blue", color = UiText.IdText(R.color.blue)),
//    ListItem(id = "", title = "yellow", color = UiText.IdText(R.color.yellow)),
//    ListItem(id = "", title = "purple", color = UiText.IdText(R.color.purple)),
//    ListItem(id = "", title = "green", color = UiText.IdText(R.color.green)),
//    ListItem(id = "", title = "red", color = UiText.IdText(R.color.red)),
//    ListItem(id = "", title = "beige", color = UiText.IdText(R.color.beige))
//)

class Colors {
    companion object {
        val names = listOf(
            "blue",
            "yellow",
            "purple",
            "green",
            "red",
            "beige"
        )

        val ids = listOf(
            R.color.blue,
            R.color.yellow,
            R.color.purple,
            R.color.green,
            R.color.red,
            R.color.beige
        )

        val colorIdOfName = names.zip(ids).toMap()

        val colorNameOfId = ids.zip(names).toMap()
    }
}