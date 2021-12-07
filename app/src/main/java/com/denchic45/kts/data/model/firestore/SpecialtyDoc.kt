package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel

data class SpecialtyDoc(
    var uuid: String,
    var name: String
) : DocModel {

    var searchKeys: List<String> = emptyList()

    private constructor() : this("", "")
}