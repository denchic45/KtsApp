package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.utils.SearchKeysGenerator

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