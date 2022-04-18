package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.ui.UiColor
import com.denchic45.kts.data.model.ui.UiImage
import com.denchic45.kts.data.model.ui.UiText
import com.google.gson.annotations.SerializedName


data class ListItem(
    @SerializedName("_id")
    override var id: String = "",
    var title: String = "",
    var icon: UiImage? = null,
    var color: UiColor? = null,
    var content: Any? = null,
    val enable: Boolean = true,
    var type: Int = 0
) : DomainModel {

    companion object {
        val emptyIcon = UiText.IdText(0)

        val emptyColor = UiText.IdText(0)
    }

    fun hasIcon(): Boolean = icon != null
}