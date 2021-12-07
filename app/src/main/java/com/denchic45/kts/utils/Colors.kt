package com.denchic45.kts.utils

import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.EitherResource
import com.denchic45.kts.data.model.domain.ListItem

val colors: List<ListItem> = listOf(
    ListItem(uuid = "", title = "blue", color = EitherResource.Id(R.color.blue)),
    ListItem(uuid = "", title = "yellow", color = EitherResource.Id(R.color.yellow)),
    ListItem(uuid = "", title = "purple", color = EitherResource.Id(R.color.purple)),
    ListItem(uuid = "", title = "green", color = EitherResource.Id(R.color.green)),
    ListItem(uuid = "", title = "red", color = EitherResource.Id(R.color.red)),
    ListItem(uuid = "", title = "beige", color = EitherResource.Id(R.color.beige))
)