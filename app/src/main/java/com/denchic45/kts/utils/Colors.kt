package com.denchic45.kts.utils

import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.EitherResource
import com.denchic45.kts.data.model.domain.ListItem

val colors: List<ListItem> = listOf(
    ListItem(id = "", title = "blue", color = EitherResource.Id(R.color.blue)),
    ListItem(id = "", title = "yellow", color = EitherResource.Id(R.color.yellow)),
    ListItem(id = "", title = "purple", color = EitherResource.Id(R.color.purple)),
    ListItem(id = "", title = "green", color = EitherResource.Id(R.color.green)),
    ListItem(id = "", title = "red", color = EitherResource.Id(R.color.red)),
    ListItem(id = "", title = "beige", color = EitherResource.Id(R.color.beige))
)