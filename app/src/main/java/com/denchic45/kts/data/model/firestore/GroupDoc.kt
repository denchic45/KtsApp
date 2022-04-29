package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class GroupDoc(
    var id: String,
    var course: Int,
    var name: String,
    var curator: UserDoc,
    @ServerTimestamp
    var timestamp: Date? = null,
    @ServerTimestamp
    var timestampCourses: Date? = null,
    var specialty: SpecialtyDoc,
    val headmanId: String?
) : DocModel {

    private constructor() : this(
        "",
        0,
        "",
        UserDoc.createEmpty(),
        null,
       null,
        SpecialtyDoc.createEmpty(),
        null
    )

    val searchKeys: List<String>
        get() = SearchKeysGenerator().generateKeys(name)

    var students: Map<String, UserDoc>? = null
        get() = field ?: emptyMap()

    @get:Exclude
    val allUsers: List<UserDoc>
        get() = students!!.values + (curator)

    companion object {
        fun createEmpty() = GroupDoc()
    }
}