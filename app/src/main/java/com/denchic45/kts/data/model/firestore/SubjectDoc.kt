package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.util.SearchKeysGenerator

class SubjectDoc(
    var id: String,
    var name: String,
    var iconUrl: String,
    var colorName: String,
) : DocModel {
    private constructor() : this(
        "",
        "",
        "",
        ""
    )

    val searchKeys: List<String>
        get() = SearchKeysGenerator().generateKeys(name, String::isNotEmpty)


    companion object {
        fun createEmpty(): SubjectDoc = SubjectDoc()
    }
}