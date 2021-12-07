package com.denchic45.kts.data.prefs

import android.content.Context
import android.util.Log
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
    var groupUuid: String
        get() = getValue(GROUP_UUID, "")
        set(groupUuid) {
            Log.d("lol", "setValue: $groupUuid")
            setValue(GROUP_UUID, groupUuid)
        }
    var groupSpecialtyUuid: String
        get() = getValue(GROUP_SPECIALTY_UUID, "")
        set(groupSpecialtyUuid) {
            setValue(GROUP_SPECIALTY_UUID, groupSpecialtyUuid)
        }

    fun saveGroupInfo(group: GroupEntity) {
        groupName = group.name
        groupCourse = group.course
        groupSpecialtyUuid = group.specialtyUuid
        groupUuid = group.uuid
    }

    companion object {
        const val GROUP_COURSE = "GROUP_COURSE"
        const val GROUP_NAME = "GROUP_NAME"
        const val GROUP_UUID = "GROUP_UUID"
        const val GROUP_SPECIALTY_UUID = "GROUP_SPECIALTY_UUID"
    }
}