package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.UiModel
import com.denchic45.kts.data.model.ui.UiColor
import com.denchic45.kts.data.model.ui.UiImage

data class OptionItem(
    val title: String = "",
    val icon: UiImage?,
    val color: UiColor?,
    val enable: Boolean = true
) : UiModel {

    fun hasIcon(): Boolean = icon != null
}