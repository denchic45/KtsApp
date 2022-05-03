package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.UiModel
import com.denchic45.kts.data.model.ui.UiColor
import com.denchic45.kts.data.model.ui.UiImage
import com.denchic45.kts.data.model.ui.UiText

data class OptionItem(
    override val id: String,
    val title: UiText,
    val icon: UiImage? = null,
    val color: UiColor? = null,
    val enable: Boolean = true
) : UiModel {

    fun hasIcon(): Boolean = icon != null
}