package com.denchic45.kts.utils

import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.OptionItem
import com.denchic45.kts.data.model.ui.UiText

class Options {
    companion object {
        fun student() = listOf(
            OptionItem(
                id = "OPTION_SET_HEADMAN",
                title = UiText.IdText(R.string.option_set_headman)
            ),
            OptionItem(
                id = "OPTION_EDIT_USER",
                title = UiText.IdText(R.string.option_edit_user)
            ),
            OptionItem(
                id = "OPTION_REMOVE_USER",
                title = UiText.IdText(R.string.option_remove_user)
            ),
        )
    }
}