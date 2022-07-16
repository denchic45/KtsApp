package com.denchic45.kts.data.model.domain

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.data.model.mapper.Default
import com.denchic45.kts.domain.model.User
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Group @Default constructor(
    override var id: String,
    var name: String,
    var course: Int,
    var specialty: Specialty,
    var curator: User
) : DomainModel {
    @ServerTimestamp
    var timestamp: Date? = null

    constructor(group: Group) : this() {
        id = group.id
        name = group.name
        course = group.course
        specialty = group.specialty.copy()
        curator = group.curator.copy()
        timestamp = group.timestamp
    }

    private constructor() : this("", "", 0, Specialty.createEmpty(), User.createEmpty())

    override fun copy(): Group {
        return Group(this)
    }

    companion object {

        fun createEmpty(): Group {
            return Group()
        }

        fun deleted(): Group {
            return Group("", "DELETED", 0, Specialty.createEmpty(), User.createEmpty())
        }
    }
}

data class GroupHeader(
    override var id: String,
    val name: String,
    val specialtyId: String
) : DomainModel {
    private constructor() : this("", "", "")

    companion object {
        fun createEmpty(): GroupHeader = GroupHeader()
    }
}
