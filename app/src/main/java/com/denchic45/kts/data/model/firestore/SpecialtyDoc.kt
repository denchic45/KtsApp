package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel

data class SpecialtyDoc(
    var id: String,
    var name: String
) : DocModel {

    var searchKeys: List<String> = emptyList()

    private constructor() : this("", "")
}