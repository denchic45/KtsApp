package com.denchic45.kts.utils

import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.OptionItem
import com.denchic45.kts.data.model.ui.UiText

class Options {
    companion object {
        fun student() = listOf(
            OptionItem(
                title = UiText.IdText(R.string.option_set_headman)
            ),
            OptionItem(
                title = UiText.IdText(R.string.option_edit_user)
            ),
            OptionItem(
                title = UiText.IdText(R.string.option_remove_headman)
            ),
        )
    }
}