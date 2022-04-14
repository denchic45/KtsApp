package com.denchic45.kts.data.model.ui

import com.denchic45.kts.data.UiModel

data class UserItem(
    override val id: String,
    val title: String,
    val photoUrl: String,
    val subtitle: String? = null
):UiModel