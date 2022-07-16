package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.util.SearchKeysGenerator

data class SpecialtyDoc(
    var id: String,
    var name: String
) : DocModel {

    val searchKeys: List<String>
        get() = SearchKeysGenerator().generateKeys(name) { predicate: String -> predicate.length > 2 }

    private constructor() : this("", "")

    companion object {
        fun createEmpty() = SpecialtyDoc()
    }
}