package com.denchic45.kts.data.prefs

import android.content.Context
import com.denchic45.kts.data.model.room.GroupEntity
import javax.inject.Inject

class GroupPreference @Inject constructor(context: Context) :
    BaseSharedPreference(context, "Group") {
    var groupCourse: Int
        get() = getValue(GROUP_COURSE, 0)
        set(course) {
            setValue(GROUP_COURSE, course)
        }
    var groupName: String
        get() = getValue(GROUP_NAME, "")
        set(groupName) {
            setValue(GROUP_NAME, groupName)
        }
    var groupId: String
        get() = getValue(GROUP_ID, "")
        set(groupId) {
            setValue(GROUP_ID, groupId)
        }
    var groupSpecialtyId: String
        get() = getValue(GROUP_SPECIALTY_ID, "")
        set(groupSpecialtyId) {
            setValue(GROUP_SPECIALTY_ID, groupSpecialtyId)
        }

    fun saveGroupInfo(group: GroupEntity) {
        groupName = group.name
        groupCourse = group.course
        groupSpecialtyId = group.specialtyId
        groupId = group.id
    }

    companion object {
        const val GROUP_COURSE = "GROUP_COURSE"
        const val GROUP_NAME = "GROUP_NAME"
        const val GROUP_ID = "GROUP_UUID"
        const val GROUP_SPECIALTY_ID = "GROUP_SPECIALTY_UUID"
    }
}