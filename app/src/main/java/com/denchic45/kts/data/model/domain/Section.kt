package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class Section(
    override var uuid: String,
    val name: String,
    val courseUuid: String
):DomainModel()
