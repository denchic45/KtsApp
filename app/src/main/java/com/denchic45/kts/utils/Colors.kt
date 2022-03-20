package com.denchic45.kts.utils

import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.EitherMessage
import com.denchic45.kts.data.model.domain.ListItem

val colors: List<ListItem> = listOf(
    ListItem(id = "", title = "blue", color = EitherMessage.Id(R.color.blue)),
    ListItem(id = "", title = "yellow", color = EitherMessage.Id(R.color.yellow)),
    ListItem(id = "", title = "purple", color = EitherMessage.Id(R.color.purple)),
    ListItem(id = "", title = "green", color = EitherMessage.Id(R.color.green)),
    ListItem(id = "", title = "red", color = EitherMessage.Id(R.color.red)),
    ListItem(id = "", title = "beige", color = EitherMessage.Id(R.color.beige))
)