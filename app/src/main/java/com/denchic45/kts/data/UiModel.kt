package com.denchic45.kts.data

import com.denchic45.kts.data.model.DomainModel

interface UiModel : DomainModel {
    override val id: String
        get() = ""
}